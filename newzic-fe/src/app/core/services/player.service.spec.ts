import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PlayerService } from './player.service';
import { SongService } from './song.service';
import { Song } from '../models';
import { environment } from '../../../environments/environment';

describe('PlayerService', () => {
  let service: PlayerService;
  let httpMock: HttpTestingController;

  const baseSong: Song = {
    id: 'song-1',
    title: 'Test Song',
    artistId: 'artist-1',
    artistName: 'Test Artist',
    artistUsername: 'testartist',
    cover: 'cover.jpg',
    duration: 200,
    genre: 'Pop',
    tags: [],
    releaseDate: '2024-01-01',
    plays: 100,
    likes: 10,
    reactions: { fire: 0, gem: 0, onpoint: 0, star: 0 },
    isExplicit: false
  };

  const fullSongResponse = {
    ...baseSong,
    audioUrl: 'data:audio/mp3;base64,MOCK_AUDIO_DATA'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PlayerService, SongService]
    });
    service = TestBed.inject(PlayerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    service.stop();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should set loading to true when playing a song without audioUrl', () => {
    const songWithoutAudio: Song = { ...baseSong, audioUrl: undefined };

    service.play(songWithoutAudio);
    expect(service.loading()).toBeTrue();
    expect(service.currentSong()?.id).toBe('song-1');

    // Flush the recordPlay POST
    const playReq = httpMock.expectOne(`${environment.apiUrl}/songs/song-1/play`);
    playReq.flush(null);

    // Flush the getById GET (lazy load)
    const getReq = httpMock.expectOne(`${environment.apiUrl}/songs/song-1`);
    expect(getReq.request.method).toBe('GET');
    getReq.flush(fullSongResponse);

    expect(service.loading()).toBeFalse();
    expect(service.currentSong()?.audioUrl).toBe('data:audio/mp3;base64,MOCK_AUDIO_DATA');
  });

  it('should NOT fetch audioUrl when song already has it', () => {
    const songWithAudio: Song = { ...baseSong, audioUrl: 'data:audio/mp3;base64,EXISTING' };

    service.play(songWithAudio);
    expect(service.loading()).toBeFalse();

    // Only recordPlay should be called, no getById
    const playReq = httpMock.expectOne(`${environment.apiUrl}/songs/song-1/play`);
    playReq.flush(null);

    // Verify no additional requests
    httpMock.expectNone(`${environment.apiUrl}/songs/song-1`);
  });

  it('should update queue with enriched song after lazy load', () => {
    const song1: Song = { ...baseSong, id: 'song-1', audioUrl: undefined };
    const song2: Song = { ...baseSong, id: 'song-2', title: 'Song 2', audioUrl: undefined };
    const queue = [song1, song2];

    service.play(song1, queue);

    // Flush recordPlay
    httpMock.expectOne(`${environment.apiUrl}/songs/song-1/play`).flush(null);
    // Flush getById for lazy load
    httpMock.expectOne(`${environment.apiUrl}/songs/song-1`).flush(fullSongResponse);

    // song-1 in queue should now have audioUrl
    const updatedQueue = service.queue();
    expect(updatedQueue[0].audioUrl).toBe('data:audio/mp3;base64,MOCK_AUDIO_DATA');
    // song-2 should still have no audioUrl
    expect(updatedQueue[1].audioUrl).toBeUndefined();
  });

  it('should cancel pending fetch when playing a new song', () => {
    const song1: Song = { ...baseSong, id: 'song-1', audioUrl: undefined };
    const song2: Song = { ...baseSong, id: 'song-2', title: 'Song 2', audioUrl: 'data:audio/mp3;base64,HAS_AUDIO' };

    // Start playing song1 (triggers lazy load)
    service.play(song1);
    expect(service.loading()).toBeTrue();

    // Before the fetch completes, switch to song2
    service.play(song2);
    expect(service.loading()).toBeFalse();
    expect(service.currentSong()?.id).toBe('song-2');

    // Flush outstanding requests without asserting on cancelled ones
    httpMock.match(() => true).forEach(req => {
      if (!req.cancelled) req.flush(null);
    });
  });

  it('should use fallback when getById returns no audioUrl', () => {
    const songWithoutAudio: Song = { ...baseSong, audioUrl: undefined, duration: 300 };

    service.play(songWithoutAudio);

    // Flush recordPlay
    httpMock.expectOne(`${environment.apiUrl}/songs/song-1/play`).flush(null);
    // Flush getById with no audioUrl
    const songResponseNoAudio = { ...baseSong, audioUrl: null };
    httpMock.expectOne(`${environment.apiUrl}/songs/song-1`).flush(songResponseNoAudio);

    expect(service.loading()).toBeFalse();
    expect(service.isPlaying()).toBeTrue();
  });
});
