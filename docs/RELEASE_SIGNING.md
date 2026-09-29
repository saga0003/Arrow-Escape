# Release Signing

Google Play expects the Android App Bundle uploaded to Play Console to be signed with your upload key.

## Keep the upload key private

Never commit `.jks`, `.keystore`, passwords, or signing-property files to this public repository. The repository's `.gitignore` excludes these file types.

## Recommended flow

1. Generate one upload keystore.
2. Keep at least two secure backups of the keystore and credentials.
3. Enable Play App Signing in Google Play Console.
4. Sign production bundles locally or store the keystore and passwords as encrypted GitHub Actions repository secrets.
5. Reuse the same upload key for future updates unless Google Play's upload-key reset process is intentionally used.

The CI workflow intentionally builds without embedding private signing credentials. This keeps the public source repository safe.
