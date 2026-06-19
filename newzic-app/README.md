# Newzic App (Capacitor)

Native Android & iOS wrapper for the Newzic Angular frontend.

## How it works

```
newzic-fe/  →  (Angular build)  →  newzic-app/www/  →  (Capacitor)  →  Android/iOS app
```

1. The Angular frontend is built as a production bundle
2. The output is copied into `www/`
3. Capacitor syncs it into the native Android/iOS projects
4. You open the native project in Android Studio or Xcode to run/build

## Setup (first time only)

```bash
cd newzic-app
npm install
npx cap add android
npx cap add ios
```

## Everyday workflow

After making changes to `newzic-fe`:

```bash
# Rebuild frontend + sync to native projects
npm run sync

# Open in Android Studio
npm run open:android

# Open in Xcode
npm run open:ios
```

## Dev mode (live reload)

For faster development, uncomment the `server.url` in `capacitor.config.ts` and point it to your local Angular dev server IP:

```ts
server: {
  url: 'http://192.168.1.X:4200',
  cleartext: true
}
```

Then run `ng serve --host 0.0.0.0` in `newzic-fe/` and `npx cap run android` here.

## Building for release

### Android
```bash
npm run sync
npm run open:android
# In Android Studio: Build → Generate Signed Bundle/APK
```

### iOS
```bash
npm run sync
npm run open:ios
# In Xcode: Product → Archive
```

## Requirements
- Node.js 18+
- Android Studio (for Android)
- Xcode 15+ (for iOS, macOS only)
- CocoaPods (`sudo gem install cocoapods`) for iOS
