export interface PremiumStatus {
  isPremium: boolean;
  premiumSince: string | null;
  customUrl: string | null;
  verified: boolean;
}

export interface PremiumLimits {
  maxSongs: number | null;
  currentSongs: number;
  maxWorkspaces: number | null;
  currentWorkspaces: number;
  maxCollaboratorsPerWorkspace: number | null;
  maxCommentsPerSong: number | null;
}

export interface Donation {
  id: string;
  fromUserId: string;
  fromUserName: string;
  fromUserAvatar: string | null;
  toArtistId: string;
  toArtistName: string;
  amountCents: number;
  artistCents: number;
  platformCents: number;
  message: string | null;
  createdAt: string;
}

export interface DonationDashboard {
  totalReceivedCents: number;
  totalSupporters: number;
  recentDonations: Donation[];
  topSupporters: TopSupporter[];
}

export interface TopSupporter {
  userId: string;
  displayName: string;
  avatar: string | null;
  totalCents: number;
}

export interface AdvancedAnalytics {
  audienceByCountry: PercentageItem[];
  audienceByGender: PercentageItem[];
  audienceByAge: PercentageItem[];
  audienceInterests: PercentageItem[];
  songPerformance: SongPerformance[];
  growth: Growth;
}

export interface PercentageItem {
  label: string;
  percent: number;
}

export interface SongPerformance {
  songId: string;
  title: string;
  cover: string | null;
  plays: number;
  likes: number;
  shares: number;
  saves: number;
  newFollowers: number;
}

export interface Growth {
  daily: GrowthPoint;
  weekly: GrowthPoint;
  monthly: GrowthPoint;
}

export interface GrowthPoint {
  plays: number;
  followers: number;
  trend: 'up' | 'down' | 'stable';
}
