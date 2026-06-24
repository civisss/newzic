import { routes } from './app.routes';

describe('App Routes', () => {
  it('should have artist route with :username parameter', () => {
    const artistRoute = routes.find(r => r.path?.startsWith('artist'));
    expect(artistRoute).toBeTruthy();
    expect(artistRoute!.path).toBe('artist/:username');
  });

  it('should NOT have artist route with :id parameter', () => {
    const artistRoute = routes.find(r => r.path === 'artist/:id');
    expect(artistRoute).toBeUndefined();
  });

  it('should have song route with :id parameter', () => {
    const songRoute = routes.find(r => r.path === 'song/:id');
    expect(songRoute).toBeTruthy();
  });
});
