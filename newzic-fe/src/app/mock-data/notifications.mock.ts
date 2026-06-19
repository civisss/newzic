import { Notification } from '../core/models';

export const MOCK_NOTIFICATIONS: Notification[] = [
  {
    id: 'n1',
    type: 'follow',
    message: 'Luna Vega started following you',
    avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop&crop=face',
    fromUser: 'Luna Vega',
    timestamp: '2024-04-15T14:30:00Z',
    read: false,
    link: '/artist/a1'
  },
  {
    id: 'n2',
    type: 'like',
    message: 'KZMA liked your track "Neon Dreams"',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face',
    fromUser: 'KZMA',
    timestamp: '2024-04-15T12:15:00Z',
    read: false,
    link: '/song/s1'
  },
  {
    id: 'n3',
    type: 'comment',
    message: 'Sora Kim commented on "Electric Bones"',
    avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=face',
    fromUser: 'Sora Kim',
    timestamp: '2024-04-14T20:45:00Z',
    read: true,
    link: '/song/s5'
  },
  {
    id: 'n4',
    type: 'release',
    message: 'DRVGN released a new album "Subsonic"',
    avatar: 'https://images.unsplash.com/photo-1463453091185-61582044d556?w=100&h=100&fit=crop&crop=face',
    fromUser: 'DRVGN',
    timestamp: '2024-04-14T10:00:00Z',
    read: true,
    link: '/artist/a5'
  },
  {
    id: 'n5',
    type: 'milestone',
    message: 'Your track "Fuego" reached 2M plays! 🔥',
    avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face',
    fromUser: 'Newzic',
    timestamp: '2024-04-13T16:30:00Z',
    read: false
  },
  {
    id: 'n6',
    type: 'follow',
    message: 'Aria Osei started following you',
    avatar: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=100&h=100&fit=crop&crop=face',
    fromUser: 'Aria Osei',
    timestamp: '2024-04-13T09:20:00Z',
    read: true,
    link: '/artist/a6'
  },
  {
    id: 'n7',
    type: 'like',
    message: 'Mila Ortiz liked your track "Golden Hour"',
    avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face',
    fromUser: 'Mila Ortiz',
    timestamp: '2024-04-12T18:50:00Z',
    read: true,
    link: '/song/s11'
  }
];
