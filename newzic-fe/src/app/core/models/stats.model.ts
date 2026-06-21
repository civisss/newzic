export interface WeeklyStats {
  label: string;
  plays: number;
  followers: number;
  reactions: number;
}

export interface ArtistStats {
  totalPlays: number;
  totalFollowers: number;
  totalFollowing: number;
  totalReactions: number;
  totalSongs: number;
  weeklyData: WeeklyStats[];
  topCities: { city: string; plays: number }[];
  growthPercent: number;
  playsTrend: 'up' | 'down' | 'stable';
  followersTrend: 'up' | 'down' | 'stable';
}
