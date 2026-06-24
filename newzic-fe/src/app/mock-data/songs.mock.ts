import { Song } from '../core/models';

export const MOCK_SONGS: Song[] = [
  {
    id: 's1', title: 'Neon Dreams', artistId: 'a1', artistName: 'Luna Vega', artistUsername: 'lunavega',
    artistAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop&crop=face',
    albumId: 'al1', albumName: 'Ethereal',
    cover: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&h=400&fit=crop',
    duration: 234, genre: 'Dream Pop', tags: ['dream-pop', 'electronic', 'vocal'],
    releaseDate: '2024-01-15', plays: 345000, likes: 12400,
    reactions: { fire: 3200, gem: 1800, onpoint: 920, star: 4100 }
  },
  {
    id: 's2', title: 'Midnight Drive', artistId: 'a1', artistName: 'Luna Vega', artistUsername: 'lunavega',
    artistAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop&crop=face',
    albumId: 'al1', albumName: 'Ethereal',
    cover: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&h=400&fit=crop',
    duration: 198, genre: 'Electro', tags: ['electro', 'synth', 'nocturnal'],
    releaseDate: '2024-01-15', plays: 280000, likes: 9800,
    reactions: { fire: 2100, gem: 1200, onpoint: 650, star: 2800 }
  },
  {
    id: 's3', title: 'Dark Matter', artistId: 'a2', artistName: 'KZMA', artistUsername: 'kzma',
    artistAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=400&h=400&fit=crop',
    duration: 175, genre: 'Trap', tags: ['trap', 'dark', 'hard-hitting', 'type-beat'],
    releaseDate: '2024-02-20', plays: 520000, likes: 18700,
    reactions: { fire: 6800, gem: 890, onpoint: 3200, star: 1500 }
  },
  {
    id: 's4', title: 'Gravity Falls', artistId: 'a2', artistName: 'KZMA', artistUsername: 'kzma',
    artistAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face',
    albumId: 'al2', albumName: 'Void',
    cover: 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=400&fit=crop',
    duration: 210, genre: 'Hip-Hop', tags: ['hip-hop', 'experimental', 'heavy'],
    releaseDate: '2024-03-10', plays: 410000, likes: 15200,
    reactions: { fire: 5100, gem: 720, onpoint: 2800, star: 1200 }
  },
  {
    id: 's5', title: 'Electric Bones', artistId: 'a3', artistName: 'The Midnight Collective', artistUsername: 'midnightcollective',
    artistAvatar: 'https://images.unsplash.com/photo-1526218626217-dc65a29bb444?w=100&h=100&fit=crop',
    albumId: 'al3', albumName: 'Wired',
    cover: 'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400&h=400&fit=crop',
    duration: 263, genre: 'Indie Rock', tags: ['indie', 'guitar', 'raw', 'live-energy'],
    releaseDate: '2024-01-28', plays: 890000, likes: 32100,
    reactions: { fire: 8900, gem: 2100, onpoint: 4500, star: 6200 }
  },
  {
    id: 's6', title: 'Rust & Gold', artistId: 'a3', artistName: 'The Midnight Collective', artistUsername: 'midnightcollective',
    artistAvatar: 'https://images.unsplash.com/photo-1526218626217-dc65a29bb444?w=100&h=100&fit=crop',
    albumId: 'al3', albumName: 'Wired',
    cover: 'https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?w=400&h=400&fit=crop',
    duration: 241, genre: 'Alternative', tags: ['alternative', 'post-punk', 'poetic'],
    releaseDate: '2024-01-28', plays: 670000, likes: 24500,
    reactions: { fire: 5600, gem: 3400, onpoint: 2100, star: 4800 }
  },
  {
    id: 's7', title: 'Cherry Blossom', artistId: 'a4', artistName: 'Sora Kim', artistUsername: 'sorakim',
    artistAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&h=400&fit=crop',
    duration: 195, genre: 'K-Pop', tags: ['kpop', 'catchy', 'dance', 'bilingual'],
    releaseDate: '2024-04-01', plays: 1200000, likes: 45600, isExplicit: false,
    reactions: { fire: 12000, gem: 8900, onpoint: 5600, star: 15000 }
  },
  {
    id: 's8', title: 'Seoul Nights', artistId: 'a4', artistName: 'Sora Kim', artistUsername: 'sorakim',
    artistAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400&h=400&fit=crop',
    duration: 222, genre: 'Pop', tags: ['pop', 'emotional', 'city-vibes'],
    releaseDate: '2024-03-15', plays: 980000, likes: 38900,
    reactions: { fire: 9800, gem: 6700, onpoint: 4200, star: 11000 }
  },
  {
    id: 's9', title: 'Deep Frequency', artistId: 'a5', artistName: 'DRVGN', artistUsername: 'drvgn',
    artistAvatar: 'https://images.unsplash.com/photo-1463453091185-61582044d556?w=100&h=100&fit=crop&crop=face',
    albumId: 'al4', albumName: 'Subsonic',
    cover: 'https://images.unsplash.com/photo-1571330735066-03aaa9429d89?w=400&h=400&fit=crop',
    duration: 387, genre: 'House', tags: ['house', 'deep', 'club', 'groove'],
    releaseDate: '2024-02-14', plays: 560000, likes: 19800,
    reactions: { fire: 4500, gem: 1200, onpoint: 3800, star: 2100 }
  },
  {
    id: 's10', title: 'Pulse', artistId: 'a5', artistName: 'DRVGN', artistUsername: 'drvgn',
    artistAvatar: 'https://images.unsplash.com/photo-1463453091185-61582044d556?w=100&h=100&fit=crop&crop=face',
    albumId: 'al4', albumName: 'Subsonic',
    cover: 'https://images.unsplash.com/photo-1508854710579-5cecc3a9ff17?w=400&h=400&fit=crop',
    duration: 342, genre: 'Techno', tags: ['techno', 'dark', 'driving', 'peak-time'],
    releaseDate: '2024-02-14', plays: 480000, likes: 17200,
    reactions: { fire: 4100, gem: 980, onpoint: 3200, star: 1800 }
  },
  {
    id: 's11', title: 'Golden Hour', artistId: 'a6', artistName: 'Aria Osei', artistUsername: 'ariaosei',
    artistAvatar: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=100&h=100&fit=crop&crop=face',
    albumId: 'al5', albumName: 'Solstice',
    cover: 'https://images.unsplash.com/photo-1501612780327-45045538702b?w=400&h=400&fit=crop',
    duration: 267, genre: 'Neo-Soul', tags: ['neo-soul', 'warm', 'vocal', 'jazz'],
    releaseDate: '2024-03-22', plays: 1500000, likes: 56700,
    reactions: { fire: 14000, gem: 12000, onpoint: 6800, star: 18000 }
  },
  {
    id: 's12', title: 'Honey & Wine', artistId: 'a6', artistName: 'Aria Osei', artistUsername: 'ariaosei',
    artistAvatar: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=100&h=100&fit=crop&crop=face',
    albumId: 'al5', albumName: 'Solstice',
    cover: 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=400&fit=crop',
    duration: 245, genre: 'R&B', tags: ['rnb', 'smooth', 'romantic', 'soulful'],
    releaseDate: '2024-03-22', plays: 1100000, likes: 42300,
    reactions: { fire: 10500, gem: 9800, onpoint: 5200, star: 13000 }
  },
  {
    id: 's13', title: 'Rainy Tape', artistId: 'a7', artistName: 'Nxmbers', artistUsername: 'nxmbers',
    artistAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1446057032654-9d8885db76c6?w=400&h=400&fit=crop',
    duration: 156, genre: 'Lo-Fi', tags: ['lofi', 'chill', 'study', 'rain'],
    releaseDate: '2024-04-05', plays: 230000, likes: 8900,
    reactions: { fire: 1800, gem: 2200, onpoint: 890, star: 3100 }
  },
  {
    id: 's14', title: 'Dusty Vinyl', artistId: 'a7', artistName: 'Nxmbers', artistUsername: 'nxmbers',
    artistAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1504898770365-14faca6a7320?w=400&h=400&fit=crop',
    duration: 168, genre: 'Boom Bap', tags: ['boom-bap', 'vinyl', 'jazz-hop', 'old-school'],
    releaseDate: '2024-03-28', plays: 190000, likes: 7200,
    reactions: { fire: 1500, gem: 1800, onpoint: 720, star: 2600 }
  },
  {
    id: 's15', title: 'Northern Lights', artistId: 'a8', artistName: 'Velvet Rust', artistUsername: 'velvetrust',
    artistAvatar: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&h=400&fit=crop',
    duration: 312, genre: 'Ambient', tags: ['ambient', 'cinematic', 'atmospheric', 'iceland'],
    releaseDate: '2024-02-08', plays: 340000, likes: 13500,
    reactions: { fire: 2100, gem: 4500, onpoint: 1200, star: 5800 }
  },
  {
    id: 's16', title: 'Glacial', artistId: 'a8', artistName: 'Velvet Rust', artistUsername: 'velvetrust',
    artistAvatar: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400&h=400&fit=crop',
    duration: 289, genre: 'Post-Rock', tags: ['post-rock', 'guitar', 'cinematic', 'crescendo'],
    releaseDate: '2024-01-20', plays: 270000, likes: 10800,
    reactions: { fire: 1800, gem: 3800, onpoint: 980, star: 4200 }
  },
  {
    id: 's17', title: 'Supernova', artistId: 'a9', artistName: 'PRXZM', artistUsername: 'prxzm',
    artistAvatar: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&h=400&fit=crop',
    duration: 228, genre: 'Future Bass', tags: ['future-bass', 'festival', 'melodic', 'drop'],
    releaseDate: '2024-04-10', plays: 450000, likes: 16800,
    reactions: { fire: 7200, gem: 1100, onpoint: 4800, star: 2900 }
  },
  {
    id: 's18', title: 'Crystallize', artistId: 'a9', artistName: 'PRXZM', artistUsername: 'prxzm',
    artistAvatar: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&h=400&fit=crop',
    duration: 256, genre: 'Dubstep', tags: ['dubstep', 'melodic', 'heavy', 'emotional'],
    releaseDate: '2024-03-30', plays: 380000, likes: 14200,
    reactions: { fire: 6100, gem: 890, onpoint: 3900, star: 2200 }
  },
  {
    id: 's19', title: 'Fuego', artistId: 'a10', artistName: 'Mila Ortiz', artistUsername: 'milaortiz',
    artistAvatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400&h=400&fit=crop',
    duration: 203, genre: 'Reggaeton', tags: ['reggaeton', 'latin', 'fire', 'dance'],
    releaseDate: '2024-04-15', plays: 2100000, likes: 78900, isExplicit: true,
    reactions: { fire: 22000, gem: 5600, onpoint: 12000, star: 18000 }
  },
  {
    id: 's20', title: 'Cielo', artistId: 'a10', artistName: 'Mila Ortiz', artistUsername: 'milaortiz',
    artistAvatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face',
    cover: 'https://images.unsplash.com/photo-1504898770365-14faca6a7320?w=400&h=400&fit=crop',
    duration: 218, genre: 'Latin Pop', tags: ['latin-pop', 'bilingual', 'summer', 'feel-good'],
    releaseDate: '2024-04-01', plays: 1800000, likes: 65400,
    reactions: { fire: 18000, gem: 7200, onpoint: 9800, star: 15000 }
  }
];
