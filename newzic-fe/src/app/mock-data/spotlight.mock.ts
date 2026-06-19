import { Spotlight } from '../core/models';

export const MOCK_SPOTLIGHTS: Spotlight[] = [
  {
    id: 'sp1',
    artistId: 'a4',
    artistName: 'Sora Kim',
    artistAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&h=300&fit=crop&crop=face',
    artistCover: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1200&h=400&fit=crop',
    quote: 'I started uploading covers from my bedroom. Now I have a million plays. Never stop creating.',
    featuredSongId: 's7',
    featuredSongTitle: 'Cherry Blossom',
    featuredSongCover: 'https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=400&h=400&fit=crop',
    editorNote: 'Sora Kim is redefining what K-pop means for independent artists. With her bilingual approach and authentic storytelling, she\'s building a global fanbase one song at a time.',
    weekLabel: 'Artist of the Week',
    artistFollowers: 48200,
    artistTotalPlays: 1250000,
    artistGenres: ['K-Pop', 'Pop', 'R&B'],
    artistTotalSongs: 12,
    artistVerified: true,
    artistLocation: 'Seoul, South Korea'
  },
  {
    id: 'sp2',
    artistId: 'a6',
    artistName: 'Aria Osei',
    artistAvatar: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=300&h=300&fit=crop&crop=face',
    artistCover: 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=1200&h=400&fit=crop',
    quote: 'Music is my language, rhythm is my poetry. Every note is a prayer from Lagos to the world.',
    featuredSongId: 's11',
    featuredSongTitle: 'Golden Hour',
    featuredSongCover: 'https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=400&h=400&fit=crop',
    editorNote: 'Aria Osei brings the depth of Yoruba traditions into neo-soul. Her album "Solstice" is a masterclass in emotional storytelling.',
    weekLabel: 'Rising Star',
    artistFollowers: 31500,
    artistTotalPlays: 890000,
    artistGenres: ['Neo-Soul', 'Afrobeat', 'R&B'],
    artistTotalSongs: 8,
    artistVerified: false,
    artistLocation: 'Lagos, Nigeria'
  }
];
