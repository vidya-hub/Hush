# Release requirements (F-Droid reproducible builds)

Hush ships on F-Droid as **developer-signed reproducible builds**. The F-Droid
infrastructure builds each release from the tagged source and compares it
byte-for-byte against the binaries attached to the matching GitHub release.
If the comparison or the signature check fails, F-Droid skips that version.

This only works while every rule below is followed.

## Non-negotiable rules

1. **The release signing keystore must be preserved permanently.** Losing it
   means F-Droid users can never receive an update again (Android refuses APKs
   signed with a different certificate). Keep at least one secure offline
   backup of the keystore and its passwords.
2. **Never rotate or regenerate the signing certificate.** The certificate
   SHA-256 is pinned in fdroiddata's `AllowedAPKSigningKeys`; a new certificate
   would strand every existing user on all channels.
3. **Every release must attach exactly these per-ABI APKs** to the GitHub
   release for the matching tag:
   - `Hush_<version>-armeabi-v7a-release.apk`
   - `Hush_<version>-x86-release.apk`
   - `Hush_<version>-x86_64-release.apk`
   - `Hush_<version>-arm64-v8a-release.apk`
   (the `universal` APK is optional and not consumed by F-Droid)
4. **APK filenames must stay compatible** with the `binary:` URL templates in
   fdroiddata's `metadata/com.vidsagar.hush.yml`. If the naming scheme ever
   changes, update that metadata in the same release.
5. **Build releases from a clean checkout with LF line endings.** The
   repository's `.gitattributes` enforces this; do not remove or override it.
   (A CRLF checkout once produced APKs that differed from F-Droid's rebuild in
   11 text assets.)
6. **Investigate reproducibility failures before publishing or changing the
   F-Droid recipe.** A quick local check: build twice from two fresh clones and
   compare the APKs' zip entries; then compare against the previous release's
   verified build to spot environment drift. Do not "fix" mismatches by
   tweaking random Gradle settings.

## Next release: 1.0.4

Continues the published `v1.0.2` version sequence with `baseVersionCode = 5`:
501 (armeabi-v7a), 502 (x86), 503 (x86_64), and 504 (arm64-v8a/universal).
Keep the existing signing certificate. Local APKs are release candidates until
the matching tag and binaries are published.

The earlier local 5.3.1 build mistakenly retained the upstream version code
110804. Android will reject 1.0.4 as an ordinary update to that local build;
export its data before any uninstall/reinstall. Published Hush 1.0.2 uses
301–304 and can update normally to this release.

## Release checklist

1. Bump `baseVersionCode` and `appVersionName` in `app/build.gradle`
   (version codes per ABI are `100 * baseVersionCode + {1,2,3,4}`).
2. Add fastlane changelogs:
   `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`.
3. Commit, tag `v<versionName>`, push `main` and the tag.
4. Build `:app:assembleRelease` from a **fresh clone** and verify the signing
   certificate SHA-256 on every APK is
   `9f01305295f8808bcda3c7eeea8f7593a9898441e65db787ded6d7bfad7a239d`.
5. Publish the GitHub release with the four per-ABI APKs (and `ffmpeg-kit.aar`
   if its pin moved).
6. Confirm F-Droid's verification passes (fdroiddata MR / reporting server)
   before announcing the release.
