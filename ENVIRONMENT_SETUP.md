# Environment Setup

## Security Notice
This project requires several API keys and sensitive configuration values. **Never commit these values directly to the repository.**

## Setup Instructions

1. **Copy the environment template:**
   ```bash
   cp env.template env
   ```

2. **Fill in your actual API keys and configuration values in the `env` file:**
   - `GEMINI_API_KEY`: Get from [Google AI Studio](https://makersuite.google.com/app/apikey)
   - `KIRI_ENGINE_API_KEY`: Contact KIRI Engine for credentials
   - `FIREBASE_PROJECT_ID`: Your Firebase project ID
   - `FIREBASE_STORAGE_BUCKET`: Your Firebase storage bucket name

3. **Ensure the `env` file is in your `.gitignore`** (already configured)

4. **For production deployment**, set these environment variables in your deployment platform rather than using the `env` file.

## Environment Variables

The application expects the following environment variables:

| Variable | Description | Required |
|----------|-------------|----------|
| `GEMINI_API_KEY` | Google Gemini API key for AI features | Yes |
| `KIRI_ENGINE_API_KEY` | KIRI Engine API key | Yes |
| `KIRI_ENGINE_BASE_URL` | KIRI Engine base URL | No (has default) |
| `FIREBASE_PROJECT_ID` | Firebase project identifier | Yes |
| `FIREBASE_STORAGE_BUCKET` | Firebase storage bucket name | Yes |
| `FIREBASE_SERVICE_ACCOUNT_FILE` | Path to Firebase service account JSON | Yes |

## Loading Environment Variables

The application automatically loads environment variables from the `env` file in the root directory. This is handled by Spring Boot's environment property resolution.

## Security Best Practices

- ✅ Use environment variables for all sensitive data
- ✅ Add sensitive files to `.gitignore`
- ✅ Use different credentials for development and production
- ✅ Regularly rotate API keys
- ❌ Never commit API keys or secrets to version control
- ❌ Never share credentials in plain text
