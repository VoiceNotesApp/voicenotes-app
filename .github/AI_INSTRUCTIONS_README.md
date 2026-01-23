# AI Instructions Documentation

## Overview

This directory contains AI instructions (`copilot-instructions.md`) that guide AI assistants (GitHub Copilot, Claude, etc.) on how to work with pull requests in this repository.

## What Are AI Instructions?

AI instructions are guidelines that help AI assistants understand:
- Repository-specific workflows and automation
- Best practices for contributing
- Quality standards and requirements
- Troubleshooting common issues

## How AI Instructions Work

### For GitHub Copilot

GitHub Copilot automatically reads the `.github/copilot-instructions.md` file and uses it to:
- Understand project context when suggesting code
- Follow repository-specific conventions
- Provide relevant assistance based on the project's needs

### For Other AI Assistants (Claude, ChatGPT, etc.)

Other AI assistants may need to be explicitly directed to read this file, but once they do, they can:
- Follow the documented workflows
- Adhere to project standards
- Provide context-aware suggestions

## Applying AI Instructions Globally

### Option 1: Per-Repository (Current Implementation)

Each repository has its own `.github/copilot-instructions.md` file:

```
repository/
  .github/
    copilot-instructions.md  <- Repository-specific instructions
    workflows/
```

**Advantages:**
- Tailored to each repository's needs
- Easy to maintain and update
- Clear versioning with git

**To replicate in other repositories:**
1. Copy `.github/copilot-instructions.md` to the target repository
2. Update repository-specific details (workflows, build commands, etc.)
3. Commit and push to the repository

### Option 2: Organization-Level (GitHub Enterprise/Pro)

For organizations with GitHub Enterprise or Pro:

1. **Create organization-level guidelines:**
   - Organization admins can set policies
   - Create `.github` repository in your organization
   - Add `copilot-instructions.md` there for org-wide defaults

2. **Repository overrides:**
   - Repository-specific instructions take precedence
   - Can extend or override org-level instructions

### Option 3: Template Repository

Create a template repository with standard AI instructions:

1. Create a template repository with `.github/copilot-instructions.md`
2. When creating new repositories, use this template
3. New repos automatically include the instructions

## Current Implementation

This repository uses **Option 1** (per-repository) with the following features:

### Automated PR Workflows

The `copilot-instructions.md` file documents:

1. **Automatic workflow triggering:**
   - Workflows run automatically on PR events
   - No manual workflow invocation needed
   - CI/CD pipeline executes lint, build, and tests

2. **Ready for review process:**
   - Clear criteria for marking PRs ready
   - Checklist of required passing checks
   - Minimal manual intervention

3. **Troubleshooting guidance:**
   - How to diagnose workflow failures
   - Common issues and solutions
   - Links to relevant logs and artifacts

## Maintaining AI Instructions

### When to Update

Update `copilot-instructions.md` when:
- Adding new workflows or automation
- Changing build processes or tools
- Updating coding standards or conventions
- Adding new quality checks or requirements

### Best Practices

1. **Keep it current:** Update instructions when workflows change
2. **Be specific:** Include exact commands, file paths, and tool versions
3. **Provide context:** Explain why things are done a certain way
4. **Include examples:** Show common scenarios and solutions
5. **Test instructions:** Verify they work with AI assistants

## Integration with Existing Workflows

The AI instructions complement existing workflows:

```
PR Created
    ↓
Workflows Auto-Trigger (pr-check.yml, android-build.yml)
    ↓
AI Assistant monitors and guides through process
    ↓
All Checks Pass
    ↓
AI Assistant can help mark as "Ready for review"
```

## Global Rollout Strategy

To implement across multiple repositories:

1. **Phase 1: Pilot** (Current)
   - Implement in autorecord-app
   - Gather feedback and refine

2. **Phase 2: Template Creation**
   - Create generic template version
   - Document customization points
   - Test with different project types

3. **Phase 3: Organization Rollout**
   - Deploy to other active repositories
   - Train team on using AI instructions
   - Monitor adoption and effectiveness

4. **Phase 4: Continuous Improvement**
   - Collect feedback from AI assistant interactions
   - Update based on new workflows or tools
   - Share learnings across repositories

## Additional Resources

- [GitHub Copilot Documentation](https://docs.github.com/en/copilot)
- [GitHub Actions Workflows](./workflows/)
- [Repository README](../README.md)

## Questions or Issues?

If AI instructions aren't working as expected:
1. Check that the file is in `.github/copilot-instructions.md`
2. Verify file formatting (markdown)
3. Ensure AI assistant has access to repository files
4. Review workflow logs for actual automation behavior

## License

These AI instructions are part of the autorecord-app project and follow the same license as the repository.
