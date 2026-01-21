package com.voicenotes.motorcycle

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.voicenotes.motorcycle.osm.OsmAuthConfig
import com.voicenotes.motorcycle.osm.OsmTokenManager
import net.openid.appauth.*

/**
 * Activity that handles OAuth 2.0 authentication flow with OpenStreetMap
 */
class OsmAuthActivity : AppCompatActivity() {
    
    private lateinit var authService: AuthorizationService
    private lateinit var tokenManager: OsmTokenManager
    
    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.data != null) {
            handleAuthorizationResponse(result.data!!)
        } else {
            finishWithError("Authorization cancelled")
        }
    }
    
    companion object {
        private const val TAG = "OsmAuthActivity"
        const val EXTRA_AUTH_COMPLETED = "auth_completed"
        const val EXTRA_AUTH_ERROR = "auth_error"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        authService = AuthorizationService(this)
        tokenManager = OsmTokenManager(this)
        
        // Check if this is a redirect from OSM
        val data: Uri? = intent.data
        if (data != null && data.scheme == "com.voicenotes.motorcycle") {
            handleAuthorizationResponse(intent)
        } else {
            // Start new authorization flow
            startAuthorizationFlow()
        }
    }
    
    private fun startAuthorizationFlow() {
        val serviceConfig = OsmAuthConfig.getServiceConfig()
        
        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            OsmAuthConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(OsmAuthConfig.REDIRECT_URI)
        )
        
        authRequestBuilder.setScope(OsmAuthConfig.SCOPE)
        val authRequest = authRequestBuilder.build()
        
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        authLauncher.launch(authIntent)
    }
    
    private fun handleAuthorizationResponse(intent: Intent) {
        val authResponse = AuthorizationResponse.fromIntent(intent)
        val authException = AuthorizationException.fromIntent(intent)
        
        if (authException != null) {
            Log.e(TAG, "Authorization failed: ${authException.message}")
            finishWithError(authException.message ?: "Authorization failed")
            return
        }
        
        if (authResponse != null) {
            val authState = AuthState(authResponse, authException)
            
            // Exchange authorization code for tokens
            authService.performTokenRequest(authResponse.createTokenExchangeRequest()) { tokenResponse, tokenException ->
                if (tokenException != null) {
                    Log.e(TAG, "Token exchange failed: ${tokenException.message}")
                    finishWithError(tokenException.message ?: "Token exchange failed")
                    return@performTokenRequest
                }
                
                if (tokenResponse != null) {
                    authState.update(tokenResponse, tokenException)
                    tokenManager.saveAuthState(authState)
                    
                    Log.d(TAG, "Authentication successful")
                    
                    // Optionally fetch user details here
                    // For now, we'll just mark as authenticated
                    
                    finishWithSuccess()
                } else {
                    finishWithError("No token received")
                }
            }
        } else {
            finishWithError("No authorization response")
        }
    }
    
    private fun finishWithSuccess() {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_AUTH_COMPLETED, true)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        Toast.makeText(this, "Connected to OpenStreetMap", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun finishWithError(error: String) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_AUTH_COMPLETED, false)
            putExtra(EXTRA_AUTH_ERROR, error)
        }
        setResult(Activity.RESULT_CANCELED, resultIntent)
        Toast.makeText(this, "Authentication failed: $error", Toast.LENGTH_LONG).show()
        finish()
    }
    
    override fun onDestroy() {
        authService.dispose()
        super.onDestroy()
    }
}
