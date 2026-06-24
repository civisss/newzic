import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';
import { ArtistComponent } from './artist.component';
import { ArtistService } from '../../core/services/artist.service';
import { SongService } from '../../core/services/song.service';
import { AlbumService } from '../../core/services/album.service';
import { PlayerService } from '../../core/services/player.service';
import { AuthService } from '../../core/services/auth.service';
import { MessageService } from '../../core/services/message.service';
import { JournalService } from '../../core/services/journal.service';

describe('ArtistComponent', () => {
  let component: ArtistComponent;
  let fixture: ComponentFixture<ArtistComponent>;
  let artistServiceSpy: jasmine.SpyObj<ArtistService>;
  let songServiceSpy: jasmine.SpyObj<SongService>;
  let albumServiceSpy: jasmine.SpyObj<AlbumService>;
  let playerServiceSpy: jasmine.SpyObj<PlayerService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let journalServiceSpy: jasmine.SpyObj<JournalService>;
  let paramsSubject: Subject<any>;

  const mockArtist = {
    id: 'artist-uuid-123',
    name: 'Test Artist',
    username: 'testartist',
    avatar: '',
    cover: '',
    bio: '',
    role: 'singer',
    genres: ['Pop'],
    tags: [],
    followers: 100,
    following: 10,
    totalPlays: 5000,
    verified: false,
    premium: false,
    joinedDate: '2024-01-01',
    socialLinks: {},
    photos: [],
    lookingForCollab: false,
    collabDescription: null,
    weeklyGrowth: null,
    country: null,
    location: null
  };

  beforeEach(async () => {
    paramsSubject = new Subject();
    artistServiceSpy = jasmine.createSpyObj('ArtistService', ['getByUsername', 'getById', 'isFollowing', 'follow']);
    songServiceSpy = jasmine.createSpyObj('SongService', ['getByArtist', 'getLikedSongs']);
    albumServiceSpy = jasmine.createSpyObj('AlbumService', ['getByArtist']);
    playerServiceSpy = jasmine.createSpyObj('PlayerService', ['play', 'togglePlay', 'currentSong', 'isPlaying'], {
      currentSong: jasmine.createSpy().and.returnValue(null),
      isPlaying: jasmine.createSpy().and.returnValue(false)
    });
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'user'], {
      isLoggedIn: jasmine.createSpy().and.returnValue(false),
      user: jasmine.createSpy().and.returnValue(null)
    });
    journalServiceSpy = jasmine.createSpyObj('JournalService', ['getPostsByAuthor']);

    artistServiceSpy.getByUsername.and.returnValue(of(mockArtist as any));
    songServiceSpy.getByArtist.and.returnValue(of([]));
    albumServiceSpy.getByArtist.and.returnValue(of([]));
    journalServiceSpy.getPostsByAuthor.and.returnValue(of({ content: [], totalPages: 0, totalElements: 0 } as any));

    await TestBed.configureTestingModule({
      imports: [ArtistComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { params: paramsSubject.asObservable() } },
        { provide: ArtistService, useValue: artistServiceSpy },
        { provide: SongService, useValue: songServiceSpy },
        { provide: AlbumService, useValue: albumServiceSpy },
        { provide: PlayerService, useValue: playerServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MessageService, useValue: {} },
        { provide: JournalService, useValue: journalServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ArtistComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should resolve artist by username from route params', fakeAsync(() => {
    fixture.detectChanges();
    paramsSubject.next({ username: 'testartist' });
    tick();

    expect(artistServiceSpy.getByUsername).toHaveBeenCalledWith('testartist');
    expect(artistServiceSpy.getById).not.toHaveBeenCalled();
    expect(component.artist()).toBeTruthy();
    expect(component.artist()!.username).toBe('testartist');
  }));

  it('should use artist ID for sub-resource loading after resolving username', fakeAsync(() => {
    fixture.detectChanges();
    paramsSubject.next({ username: 'testartist' });
    tick();

    expect(songServiceSpy.getByArtist).toHaveBeenCalledWith('artist-uuid-123');
    expect(albumServiceSpy.getByArtist).toHaveBeenCalledWith('artist-uuid-123');
  }));

  it('should not load sub-resources if artist not found', fakeAsync(() => {
    artistServiceSpy.getByUsername.and.returnValue(of(undefined));
    fixture.detectChanges();
    paramsSubject.next({ username: 'nonexistent' });
    tick();

    expect(component.artist()).toBeNull();
    expect(songServiceSpy.getByArtist).not.toHaveBeenCalled();
    expect(albumServiceSpy.getByArtist).not.toHaveBeenCalled();
  }));
});
