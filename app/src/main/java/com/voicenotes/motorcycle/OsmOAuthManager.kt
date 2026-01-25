package com.voicenotes.motorcycle

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * OsmOAuthManager handles OAuth 2.0 authentication with OpenStreetMap.
 * 
 * OAuth Redirect URI Configuration:
 * =================================
 * This manager uses the redirect URI: app.voicenotes.motorcycle://oauth
 * 
 * The redirect URI must match:
 * 1. The REDIRECT_URI constant below
 * 2. The intent-filter in AndroidManifest.xml (SettingsActivity)
 * 3. The redirect URI registered in your OSM OAuth application
 * 
 * For Secondary Apps or Debug Variants:
 * ======================================
 * If you're building a separate "manage" app or debug variant that also needs
 * OpenStreetMap OAuth, you should:
 * 
 * 1. Change the REDIRECT_URI below to a unique scheme for that variant:
 *    Example: "app.voicenotes-manage.motorcycle://oauth"
 *             "app.voicenotes.debug.motorcycle://oauth"
 * 
 * 2. Update the AndroidManifest.xml intent-filter for that variant to match
 * 
 * 3. Register a separate OAuth application on OpenStreetMap with the new redirect URI
 * 
 * 4. Use build variants or product flavors to maintain separate configurations
 * 
 * This ensures each app has its own OAuth handler, preventing Android from showing
 * multiple app options during the OAuth flow (the "two icons" problem).
 * 
 * See AndroidManifest.xml for more details on the intent-filter configuration.
 */
class OsmOAuthManager(private val context: Context) {
    
    companion object {
        private const val OSM_AUTH_ENDPOINT = "https://www.openstreetmap.org/oauth2/authorize"
        private const val OSM_TOKEN_ENDPOINT = "https://www.openstreetmap.org/oauth2/token"
        private const val OSM_USER_DETAILS_ENDPOINT = "https://api.openstreetmap.org/api/0.6/user/details.json"
        private val CLIENT_ID = BuildConfig.OSM_CLIENT_ID
        
        /**
         * OAuth 2.0 Redirect URI
         * 
         * This MUST match:
         * - The intent-filter scheme/host in AndroidManifest.xml (SettingsActivity)
         * - The redirect URI configured in your OpenStreetMap OAuth application
         * 
         * Current value: app.voicenotes.motorcycle://oauth
         * 
         * For different app variants (debug, manage, etc.), change this to a unique
         * scheme to avoid conflicts. See class documentation for details.
         */
        private const val REDIRECT_URI = "app.voicenotes.motorcycle://oauth"
        const val DEFAULT_CLIENT_ID_PLACEHOLDER = "your_osm_client_id"
        
        private const val PREF_ACCESS_TOKEN = "osm_access_token"
        private const val PREF_REFRESH_TOKEN = "osm_refresh_token"
        private const val PREF_USERNAME = "osm_username"
    }
    
    private val authService = AuthorizationService(context)
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    fun startOAuthFlow(launcher: ActivityResultLauncher<Intent>) {
        if (CLIENT_ID.isBlank() || CLIENT_ID == DEFAULT_CLIENT_ID_PLACEHOLDER) {
            throw IllegalStateException("OSM Client ID not configured")
        }
        
        DebugLogger.logInfo(
            service = "OSM OAuth",
            message = "Starting OAuth flow with client ID: ${CLIENT_ID.take(10)}..."
        )
        
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse(OSM_AUTH_ENDPOINT),
            Uri.parse(OSM_TOKEN_ENDPOINT)
        )
        
        // Generate code verifier for PKCE
        val codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier()
        
        val authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI)
        )
            .setScope("read_prefs write_notes")
            .setCodeVerifier(codeVerifier)  // Enable PKCE
            .build()
        
        DebugLogger.logApiRequest(
            service = "OSM OAuth",
            method = "GET",
            url = OSM_AUTH_ENDPOINT,
            headers = mapOf("scope" to "read_prefs write_notes")
        )
        
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        launcher.launch(authIntent)
    }
    
    fun handleOAuthResponse(intent: Intent, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)
        
        if (response != null) {
            DebugLogger.logInfo(
                service = "OSM OAuth",
                message = "Authorization code received, exchanging for token"
            )
            
            // Exchange code for token
            authService.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, ex ->
                if (tokenResponse != null) {
                    val accessToken = tokenResponse.accessToken ?: ""
                    val refreshToken = tokenResponse.refreshToken ?: ""
                    
                    DebugLogger.logApiResponse(
                        service = "OSM OAuth",
                        statusCode = 200,
                        responseBody = "Token exchange successful"
                    )
                    
                    // Save tokens securely
                    saveTokensToKeystore(accessToken, refreshToken)
                    
                    // Fetch username
                    fetchUsername(accessToken) { username ->
                        saveUsername(username)
                        onSuccess(username)
                    }
                } else {
                    DebugLogger.logError(
                        service = "OSM OAuth",
                        error = "Token exchange failed",
                        exception = ex
                    )
                    onFailure(ex ?: Exception("Token exchange failed"))
                }
            }
        } else {
            DebugLogger.logError(
                service = "OSM OAuth",
                error = "Authorization failed",
                exception = exception
            )
            onFailure(exception ?: Exception("Authorization failed"))
        }
    }
    
    private fun fetchUsername(accessToken: String, callback: (String) -> Unit) {
        // Call OSM API to get user details in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = OSM_USER_DETAILS_ENDPOINT
                
                // Log the request
                DebugLogger.logApiRequest(
                    service = "OSM User API",
                    method = "GET",
                    url = url,
                    headers = mapOf("Authorization" to "Bearer ***")
                )
                
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
                
                val response = httpClient.newCall(request).execute()
                
                response.use {
                    val responseBody = it.body?.string()
                    
                    if (it.isSuccessful && responseBody != null) {
                        // Parse JSON response to extract display_name
                        val jsonObject = JSONObject(responseBody)
                        
                        // Safely extract display_name with null checks
                        val userObject = jsonObject.optJSONObject("user")
                        val displayName = userObject?.optString("display_name")
                        
                        if (displayName != null && displayName.isNotBlank()) {
                            Log.d("OsmOAuthManager", "Fetched username: $displayName")
                            DebugLogger.logApiResponse(
                                service = "OSM User API",
                                statusCode = it.code,
                                responseBody = "Username fetched successfully: $displayName"
                            )
                            
                            // Call callback on the main thread
                            withContext(Dispatchers.Main) {
                                callback(displayName)
                            }
                        } else {
                            val error = "display_name not found in API response"
                            Log.e("OsmOAuthManager", error)
                            DebugLogger.logApiResponse(
                                service = "OSM User API",
                                statusCode = it.code,
                                responseBody = responseBody,
                                error = error
                            )
                            
                            // Fall back to default username
                            withContext(Dispatchers.Main) {
                                callback("OSM_User")
                            }
                        }
                    } else {
                        val error = "Failed to fetch username: ${it.code} ${it.message}"
                        Log.e("OsmOAuthManager", error)
                        DebugLogger.logApiResponse(
                            service = "OSM User API",
                            statusCode = it.code,
                            responseBody = responseBody,
                            error = error
                        )
                        
                        // Fall back to default username on error
                        withContext(Dispatchers.Main) {
                            callback("OSM_User")
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("OsmOAuthManager", "Network error fetching username", e)
                DebugLogger.logError(
                    service = "OSM User API",
                    error = "Network error fetching username",
                    exception = e
                )
                
                // Fall back to default username on network error
                withContext(Dispatchers.Main) {
                    callback("OSM_User")
                }
            } catch (e: JSONException) {
                Log.e("OsmOAuthManager", "Error parsing JSON response", e)
                DebugLogger.logError(
                    service = "OSM User API",
                    error = "Error parsing JSON response",
                    exception = e
                )
                
                // Fall back to default username on JSON parse error
                withContext(Dispatchers.Main) {
                    callback("OSM_User")
                }
            } catch (e: Exception) {
                Log.e("OsmOAuthManager", "Unexpected error fetching username", e)
                DebugLogger.logError(
                    service = "OSM User API",
                    error = "Unexpected error fetching username",
                    exception = e
                )
                
                // Fall back to default username on unexpected error
                withContext(Dispatchers.Main) {
                    callback("OSM_User")
                }
            }
        }
    }
    
    fun saveTokensToKeystore(accessToken: String, refreshToken: String) {
        // Store tokens in SharedPreferences
        // NOTE: For production apps, consider using Android Keystore for enhanced security
        val prefs = context.getSharedPreferences("OsmAuth", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(PREF_ACCESS_TOKEN, accessToken)
            putString(PREF_REFRESH_TOKEN, refreshToken)
            apply()
        }
        Log.d("OsmOAuthManager", "Tokens saved to storage")
    }
    
    fun getAccessToken(): String? {
        val prefs = context.getSharedPreferences("OsmAuth", Context.MODE_PRIVATE)
        return prefs.getString(PREF_ACCESS_TOKEN, null)
    }
    
    fun getUsername(): String? {
        val prefs = context.getSharedPreferences("OsmAuth", Context.MODE_PRIVATE)
        return prefs.getString(PREF_USERNAME, null)
    }
    
    fun saveUsername(username: String) {
        val prefs = context.getSharedPreferences("OsmAuth", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_USERNAME, username).apply()
    }
    
    fun removeTokens() {
        val prefs = context.getSharedPreferences("OsmAuth", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Log.d("OsmOAuthManager", "Tokens removed")
    }
    
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }
}
