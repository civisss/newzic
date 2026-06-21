import { ArtistStats } from '../core/models';

export const MOCK_STATS: ArtistStats = {
  totalPlays: 45200,
  totalFollowers: 1280,
  totalFollowing: 67,
  totalReactions: 8900,
  totalSongs: 6,
  weeklyData: [
    { label: 'Mon', plays: 1200, followers: 12, reactions: 340 },
    { label: 'Tue', plays: 1800, followers: 18, reactions: 520 },
    { label: 'Wed', plays: 2400, followers: 24, reactions: 680 },
    { label: 'Thu', plays: 1600, followers: 15, reactions: 450 },
    { label: 'Fri', plays: 3200, followers: 32, reactions: 890 },
    { label: 'Sat', plays: 4100, followers: 45, reactions: 1200 },
    { label: 'Sun', plays: 3500, followers: 38, reactions: 980 }
  ],
  topCities: [
    { city: 'Berlin', plays: 8200 },
    { city: 'London', plays: 6800 },
    { city: 'New York', plays: 5400 },
    { city: 'Tokyo', plays: 4200 },
    { city: 'Los Angeles', plays: 3800 }
  ],
  growthPercent: 14.2,
  playsTrend: 'up',
  followersTrend: 'up'
};
