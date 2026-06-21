import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SongService } from './song.service';
import { environment } from '../../../environments/environment';

describe('SongService', () => {
  let service: SongService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SongService]
    });
    service = TestBed.inject(SongService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should toggle like on a song', () => {
    service.toggleLike('song-1').subscribe(res => {
      expect(res.liked).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/songs/song-1/like`);
    expect(req.request.method).toBe('POST');
    req.flush({ liked: true });
  });

  it('should check if song is liked', () => {
    service.isLiked('song-1').subscribe(res => {
      expect(res.liked).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/songs/song-1/liked`);
    expect(req.request.method).toBe('GET');
    req.flush({ liked: true });
  });

  it('should get liked songs list', () => {
    const mockSongs = [
      {
        id: 'song-1', title: 'Test Song', artistId: 'a1', artistName: 'Artist',
        artistAvatar: '', albumId: null, albumName: null, cover: '',
        duration: 200, genre: 'Pop', tags: [], releaseDate: '2024-01-01',
        plays: 100, likes: 10, reactions: { fire: 0, gem: 0, onpoint: 0, star: 0 },
        audioUrl: '', isExplicit: false
      }
    ];

    service.getLikedSongs().subscribe(songs => {
      expect(songs.length).toBe(1);
      expect(songs[0].title).toBe('Test Song');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/songs/liked`);
    expect(req.request.method).toBe('GET');
    req.flush(mockSongs);
  });

  it('should react to a song', () => {
    service.react('song-1', 'fire').subscribe(res => {
      expect(res.added).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/songs/song-1/react?type=fire`);
    expect(req.request.method).toBe('POST');
    req.flush({ added: true });
  });
});
