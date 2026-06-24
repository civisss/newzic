import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ArtistService } from './artist.service';
import { environment } from '../../../environments/environment';

describe('ArtistService', () => {
  let service: ArtistService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ArtistService]
    });
    service = TestBed.inject(ArtistService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get artist by ID', () => {
    const mockArtist = {
      id: 'u1', displayName: 'Test Artist', username: 'testartist', avatar: '', cover: '',
      bio: '', roles: ['singer'], followers: 100, following: 10, totalPlays: 5000,
      genres: ['Pop'], tags: [], verified: false, premium: false, joinedDate: '2024-01-01',
      socialLinks: {}, photos: [], preferredGenres: [], lookingForCollab: false,
      collabDescription: null, weeklyGrowth: null, country: null, location: null,
      preferredLanguage: null, longBio: null
    };

    service.getById('u1').subscribe(artist => {
      expect(artist).toBeTruthy();
      expect(artist!.id).toBe('u1');
      expect(artist!.username).toBe('testartist');
      expect(artist!.name).toBe('Test Artist');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/artists/u1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockArtist);
  });

  it('should get artist by username', () => {
    const mockArtist = {
      id: 'u1', displayName: 'Test Artist', username: 'testartist', avatar: '', cover: '',
      bio: '', roles: ['singer'], followers: 100, following: 10, totalPlays: 5000,
      genres: ['Pop'], tags: [], verified: false, premium: false, joinedDate: '2024-01-01',
      socialLinks: {}, photos: [], preferredGenres: [], lookingForCollab: false,
      collabDescription: null, weeklyGrowth: null, country: null, location: null,
      preferredLanguage: null, longBio: null
    };

    service.getByUsername('testartist').subscribe(artist => {
      expect(artist).toBeTruthy();
      expect(artist!.id).toBe('u1');
      expect(artist!.username).toBe('testartist');
      expect(artist!.name).toBe('Test Artist');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/artists/by-username/testartist`);
    expect(req.request.method).toBe('GET');
    req.flush(mockArtist);
  });

  it('should encode special characters in username for getByUsername', () => {
    const mockArtist = {
      id: 'u2', displayName: 'Special User', username: 'user@name', avatar: '', cover: '',
      bio: '', roles: ['singer'], followers: 0, following: 0, totalPlays: 0,
      genres: [], tags: [], verified: false, premium: false, joinedDate: '2024-01-01',
      socialLinks: {}, photos: [], preferredGenres: [], lookingForCollab: false,
      collabDescription: null, weeklyGrowth: null, country: null, location: null,
      preferredLanguage: null, longBio: null
    };

    service.getByUsername('user@name').subscribe(artist => {
      expect(artist).toBeTruthy();
      expect(artist!.username).toBe('user@name');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/artists/by-username/user%40name`);
    expect(req.request.method).toBe('GET');
    req.flush(mockArtist);
  });

  it('should follow an artist', () => {
    service.follow('artist-1').subscribe(res => {
      expect(res.following).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/artists/artist-1/follow`);
    expect(req.request.method).toBe('POST');
    req.flush({ following: true });
  });

  it('should check if following an artist', () => {
    service.isFollowing('artist-1').subscribe(res => {
      expect(res.following).toBeFalse();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/artists/artist-1/following`);
    expect(req.request.method).toBe('GET');
    req.flush({ following: false });
  });

  it('should get followers list', () => {
    const mockFollowers = [
      {
        id: 'u1', displayName: 'Fan User', username: 'fan', avatar: '', cover: '',
        bio: '', roles: ['singer'], followers: 10, following: 5, totalPlays: 100,
        genres: ['Pop'], tags: [], verified: false, premium: false, joinedDate: '2024-01-01',
        socialLinks: {}, photos: [], preferredGenres: [], lookingForCollab: false,
        collabDescription: null, weeklyGrowth: null, country: null, location: null,
        preferredLanguage: null, longBio: null
      }
    ];

    service.getFollowers('artist-1').subscribe(list => {
      expect(list.length).toBe(1);
      expect(list[0].name).toBe('Fan User');
      expect(list[0].followers).toBe(10);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/artists/artist-1/followers`);
    expect(req.request.method).toBe('GET');
    req.flush(mockFollowers);
  });

  it('should get following list', () => {
    const mockFollowing = [
      {
        id: 'u2', displayName: 'Star Artist', username: 'star', avatar: '', cover: '',
        bio: '', roles: ['producer'], followers: 5000, following: 20, totalPlays: 50000,
        genres: ['Trap'], tags: [], verified: true, premium: false, joinedDate: '2023-06-01',
        socialLinks: {}, photos: [], preferredGenres: [], lookingForCollab: false,
        collabDescription: null, weeklyGrowth: 5.2, country: 'IT', location: 'Milan',
        preferredLanguage: 'it', longBio: null
      }
    ];

    service.getFollowing('artist-1').subscribe(list => {
      expect(list.length).toBe(1);
      expect(list[0].name).toBe('Star Artist');
      expect(list[0].verified).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/artists/artist-1/following-list`);
    expect(req.request.method).toBe('GET');
    req.flush(mockFollowing);
  });

  it('should return empty followers list', () => {
    service.getFollowers('artist-empty').subscribe(list => {
      expect(list.length).toBe(0);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/artists/artist-empty/followers`);
    req.flush([]);
  });

  it('should return empty following list', () => {
    service.getFollowing('artist-empty').subscribe(list => {
      expect(list.length).toBe(0);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/artists/artist-empty/following-list`);
    req.flush([]);
  });
});
