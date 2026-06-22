import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.newzic.app',
  appName: 'Newzic',
  webDir: 'www',
  server: {
    // In development, point to your local Angular dev server:
    // url: 'http://192.168.1.X:4200',
    // cleartext: true,

    // In production, it uses the bundled www/ folder
    androidScheme: 'https',
    iosScheme: 'https'
  },
  plugins: {
    CapacitorHttp: {
      enabled: false
    },
    SplashScreen: {
      launchAutoHide: true,
      launchShowDuration: 2000,
      backgroundColor: '#0a0a0f',
      showSpinner: false
    },
    StatusBar: {
      style: 'DARK',
      backgroundColor: '#0a0a0f'
    },
    Keyboard: {
      resize: 'body',
      resizeOnFullScreen: true
    }
  }
};

export default config;
