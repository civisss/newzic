import { Spotlight } from '../core/models';

export const MOCK_SPOTLIGHTS: Spotlight[] = [
  {
    artistId: 'a4',
    artistName: 'Sora Kim',
    artistUsername: 'sorakim',
    artistAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&h=300&fit=crop&crop=face',
    artistCover: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1200&h=400&fit=crop',
    artistFollowers: 48200,
    artistTotalPlays: 1250000,
    artistGenres: ['K-Pop', 'Pop', 'R&B'],
    artistTotalSongs: 12,
    artistVerified: true,
    artistLocation: 'Seoul, South Korea',
    category: 'top_plays',
    categoryLabel: '🔥 Most Played Artist',
    description: 'Sora Kim is redefining what K-pop means for independent artists.'
  },
  {
    artistId: 'a6',
    artistName: 'Aria Osei',
    artistUsername: 'ariaosei',
    artistAvatar: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=300&h=300&fit=crop&crop=face',
    artistCover: 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=1200&h=400&fit=crop',
    artistFollowers: 31500,
    artistTotalPlays: 890000,
    artistGenres: ['Neo-Soul', 'Afrobeat', 'R&B'],
    artistTotalSongs: 8,
    artistVerified: false,
    artistLocation: 'Lagos, Nigeria',
    category: 'top_reactions',
    categoryLabel: '💎 Most Loved Artist',
    description: 'Aria Osei brings the depth of Yoruba traditions into neo-soul.',
    extraStat: 15400
  }
];
