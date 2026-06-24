import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { JournalService } from './journal.service';
import { environment } from '../../../environments/environment';

describe('JournalService', () => {
  let service: JournalService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/journal`;

  const mockPost = {
    id: 'post-1',
    authorId: 'user-1',
    authorName: 'Test Artist',
    authorUsername: 'testartist',
    authorAvatar: null,
    authorRole: 'singer',
    authorPremium: false,
    authorVerified: false,
    content: 'Working on new beats! #lofi',
    imageUrl: null,
    category: 'update',
    hashtags: ['lofi'],
    taggedUsers: [],
    reactions: { total: 0, like: 0, fire: 0, music: 0, hype: 0, userReactions: [] },
    commentCount: 0,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  };

  const mockComment = {
    id: 'comment-1',
    postId: 'post-1',
    authorId: 'user-2',
    authorName: 'Fan',
    authorUsername: 'fan',
    authorAvatar: null,
    content: 'Great post!',
    createdAt: '2024-01-01T00:00:00Z'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [JournalService]
    });
    service = TestBed.inject(JournalService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ── Posts ──

  it('should create a journal post', () => {
    const request = { content: 'Hello world', category: 'update' };

    service.createPost(request).subscribe(post => {
      expect(post.id).toBe('post-1');
      expect(post.content).toBe('Working on new beats! #lofi');
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockPost);
  });

  it('should update a journal post', () => {
    const update = { content: 'Updated content' };

    service.updatePost('post-1', update).subscribe(post => {
      expect(post.id).toBe('post-1');
    });

    const req = httpMock.expectOne(`${baseUrl}/post-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(update);
    req.flush(mockPost);
  });

  it('should delete a journal post', () => {
    service.deletePost('post-1').subscribe();

    const req = httpMock.expectOne(`${baseUrl}/post-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should get a single journal post', () => {
    service.getPost('post-1').subscribe(post => {
      expect(post.content).toBe('Working on new beats! #lofi');
    });

    const req = httpMock.expectOne(`${baseUrl}/post-1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPost);
  });

  it('should get posts by author', () => {
    const pagedResponse = { content: [mockPost], totalElements: 1, totalPages: 1, number: 0, size: 20, last: true };

    service.getPostsByAuthor('user-1').subscribe(res => {
      expect(res.content.length).toBe(1);
      expect(res.totalElements).toBe(1);
      expect(res.content[0].authorId).toBe('user-1');
    });

    const req = httpMock.expectOne(`${baseUrl}/user/user-1?page=0&size=20`);
    expect(req.request.method).toBe('GET');
    req.flush(pagedResponse);
  });

  it('should get posts by author with custom pagination', () => {
    const pagedResponse = { content: [], totalElements: 0, totalPages: 0, number: 2, size: 5, last: true };

    service.getPostsByAuthor('user-1', 2, 5).subscribe(res => {
      expect(res.number).toBe(2);
      expect(res.size).toBe(5);
    });

    const req = httpMock.expectOne(`${baseUrl}/user/user-1?page=2&size=5`);
    expect(req.request.method).toBe('GET');
    req.flush(pagedResponse);
  });

  // ── Feed ──

  it('should get journal feed', () => {
    const pagedResponse = { content: [mockPost], totalElements: 1, totalPages: 1, number: 0, size: 20, last: true };

    service.getFeed().subscribe(res => {
      expect(res.content.length).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/feed?page=0&size=20`);
    expect(req.request.method).toBe('GET');
    req.flush(pagedResponse);
  });

  it('should get journal feed with custom pagination', () => {
    const pagedResponse = { content: [], totalElements: 0, totalPages: 0, number: 1, size: 10, last: true };

    service.getFeed(1, 10).subscribe(res => {
      expect(res.number).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/feed?page=1&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(pagedResponse);
  });

  // ── Search ──

  it('should search journal posts by text', () => {
    const pagedResponse = { content: [mockPost], totalElements: 1, totalPages: 1, number: 0, size: 20, last: true };

    service.search('beats').subscribe(res => {
      expect(res.content.length).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/search?q=beats&page=0&size=20`);
    expect(req.request.method).toBe('GET');
    req.flush(pagedResponse);
  });

  it('should search journal posts by hashtag', () => {
    const pagedResponse = { content: [mockPost], totalElements: 1, totalPages: 1, number: 0, size: 20, last: true };

    service.search('#lofi').subscribe(res => {
      expect(res.content.length).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/search?q=%23lofi&page=0&size=20`);
    expect(req.request.method).toBe('GET');
    req.flush(pagedResponse);
  });

  // ── Reactions ──

  it('should toggle a reaction on a post', () => {
    const reactionSummary = { total: 1, like: 0, fire: 1, music: 0, hype: 0, userReactions: ['fire'] };

    service.toggleReaction('post-1', 'fire').subscribe(res => {
      expect(res.total).toBe(1);
      expect(res.fire).toBe(1);
      expect(res.userReactions).toContain('fire');
    });

    const req = httpMock.expectOne(`${baseUrl}/post-1/reactions`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ type: 'fire' });
    req.flush(reactionSummary);
  });

  // ── Comments ──

  it('should get comments for a post', () => {
    const pagedResponse = { content: [mockComment], totalElements: 1, totalPages: 1, number: 0, size: 50, last: true };

    service.getComments('post-1').subscribe(res => {
      expect(res.content.length).toBe(1);
      expect(res.content[0].content).toBe('Great post!');
    });

    const req = httpMock.expectOne(`${baseUrl}/post-1/comments?page=0&size=50`);
    expect(req.request.method).toBe('GET');
    req.flush(pagedResponse);
  });

  it('should add a comment to a post', () => {
    service.addComment('post-1', 'Nice work!').subscribe(comment => {
      expect(comment.content).toBe('Great post!');
    });

    const req = httpMock.expectOne(`${baseUrl}/post-1/comments`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ content: 'Nice work!' });
    req.flush(mockComment);
  });

  it('should delete a comment', () => {
    service.deleteComment('comment-1').subscribe();

    const req = httpMock.expectOne(`${baseUrl}/comments/comment-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  // ── Error handling (402 Premium limit) ──

  it('should propagate 402 error when free post limit is reached', () => {
    const request = { content: 'Too many posts' };

    service.createPost(request).subscribe({
      next: () => fail('should have failed'),
      error: (err) => {
        expect(err.status).toBe(402);
        expect(err.error.limitType).toBe('journal_posts');
      }
    });

    const req = httpMock.expectOne(baseUrl);
    req.flush(
      { error: 'Free users can publish a maximum of 10 journal posts.', limitType: 'journal_posts' },
      { status: 402, statusText: 'Payment Required' }
    );
  });
});
