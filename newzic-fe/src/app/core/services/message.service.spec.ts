import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MessageService, ConversationPreview, Message } from './message.service';
import { environment } from '../../../environments/environment';

describe('MessageService', () => {
  let service: MessageService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MessageService]
    });
    service = TestBed.inject(MessageService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should load conversations', () => {
    const mockConversations: ConversationPreview[] = [
      {
        userId: '123',
        displayName: 'Test User',
        avatar: null,
        lastMessage: 'Hello!',
        lastMessageTime: '2024-01-01T00:00:00',
        unread: true
      }
    ];

    service.loadConversations();
    const req = httpMock.expectOne(`${environment.apiUrl}/messages/conversations`);
    expect(req.request.method).toBe('GET');
    req.flush(mockConversations);

    expect(service.conversations().length).toBe(1);
    expect(service.conversations()[0].displayName).toBe('Test User');
  });

  it('should load unread count', () => {
    service.loadUnreadCount();
    const req = httpMock.expectOne(`${environment.apiUrl}/messages/unread-count`);
    expect(req.request.method).toBe('GET');
    req.flush({ count: 3 });

    expect(service.unreadCount()).toBe(3);
  });

  it('should send a message', () => {
    const mockMessage: Message = {
      id: 'msg-1',
      senderId: 'user-1',
      senderName: 'Sender',
      senderAvatar: null,
      recipientId: 'user-2',
      recipientName: 'Recipient',
      recipientAvatar: null,
      content: 'Hey!',
      isRead: false,
      createdAt: '2024-01-01T00:00:00'
    };

    service.sendMessage('user-2', 'Hey!').subscribe(msg => {
      expect(msg.content).toBe('Hey!');
      expect(msg.recipientId).toBe('user-2');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/messages/send/user-2`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ content: 'Hey!' });
    req.flush(mockMessage);
  });

  it('should get conversation messages', () => {
    const mockMessages: Message[] = [
      {
        id: 'msg-1', senderId: 'user-1', senderName: 'A', senderAvatar: null,
        recipientId: 'user-2', recipientName: 'B', recipientAvatar: null,
        content: 'Hi!', isRead: true, createdAt: '2024-01-01T00:00:00'
      },
      {
        id: 'msg-2', senderId: 'user-2', senderName: 'B', senderAvatar: null,
        recipientId: 'user-1', recipientName: 'A', recipientAvatar: null,
        content: 'Hello!', isRead: false, createdAt: '2024-01-01T00:01:00'
      }
    ];

    service.getConversation('user-2').subscribe(msgs => {
      expect(msgs.length).toBe(2);
      expect(msgs[0].content).toBe('Hi!');
      expect(msgs[1].content).toBe('Hello!');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/messages/conversation/user-2`);
    expect(req.request.method).toBe('GET');
    req.flush(mockMessages);
  });

  it('should mark conversation as read', () => {
    service.markAsRead('user-2').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/messages/read/user-2`);
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });
});
