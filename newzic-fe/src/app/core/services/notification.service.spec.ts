import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NotificationService } from './notification.service';
import { environment } from '../../../environments/environment';

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [NotificationService]
    });
    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should load notifications and unread count', () => {
    const mockNotifications = {
      content: [
        {
          id: 'n1', type: 'follow', message: 'User started following you',
          avatar: 'https://example.com/avatar.jpg', fromUserId: 'u1',
          fromUser: 'User', songId: null, timestamp: '2024-01-01T12:00:00',
          read: false, link: '/artist/u1'
        },
        {
          id: 'n2', type: 'reaction', message: 'User reacted 🔥 to "Song"',
          avatar: '', fromUserId: 'u2', fromUser: 'Fan',
          songId: 's1', timestamp: '2024-01-02T12:00:00',
          read: true, link: '/artist/a1'
        }
      ]
    };

    service.load();

    const notifReq = httpMock.expectOne(`${environment.apiUrl}/notifications`);
    expect(notifReq.request.method).toBe('GET');
    notifReq.flush(mockNotifications);

    const countReq = httpMock.expectOne(`${environment.apiUrl}/notifications/unread-count`);
    expect(countReq.request.method).toBe('GET');
    countReq.flush({ count: 3 });

    expect(service.notifications().length).toBe(2);
    expect(service.notifications()[0].type).toBe('follow');
    expect(service.notifications()[0].fromUserId).toBe('u1');
    expect(service.notifications()[1].songId).toBe('s1');
    expect(service.unreadCount()).toBe(3);
  });

  it('should mark all as read', () => {
    service.markAllAsRead();

    const req = httpMock.expectOne(`${environment.apiUrl}/notifications/mark-read`);
    expect(req.request.method).toBe('POST');
    req.flush({});

    expect(service.unreadCount()).toBe(0);
  });

  it('should mark single notification as read locally', () => {
    // First load some notifications
    service.load();

    const notifReq = httpMock.expectOne(`${environment.apiUrl}/notifications`);
    notifReq.flush({
      content: [
        {
          id: 'n1', type: 'follow', message: 'test',
          avatar: '', fromUser: 'User', timestamp: '2024-01-01T12:00:00',
          read: false, link: null
        }
      ]
    });

    const countReq = httpMock.expectOne(`${environment.apiUrl}/notifications/unread-count`);
    countReq.flush({ count: 1 });

    expect(service.notifications()[0].read).toBeFalse();

    service.markAsRead('n1');

    const markReq = httpMock.expectOne(`${environment.apiUrl}/notifications/n1/mark-read`);
    expect(markReq.request.method).toBe('POST');
    markReq.flush({});

    expect(service.notifications()[0].read).toBeTrue();
    expect(service.unreadCount()).toBe(0);
  });

  it('should handle empty notification list', () => {
    service.load();

    const notifReq = httpMock.expectOne(`${environment.apiUrl}/notifications`);
    notifReq.flush({ content: [] });

    const countReq = httpMock.expectOne(`${environment.apiUrl}/notifications/unread-count`);
    countReq.flush({ count: 0 });

    expect(service.notifications().length).toBe(0);
    expect(service.unreadCount()).toBe(0);
  });
});
