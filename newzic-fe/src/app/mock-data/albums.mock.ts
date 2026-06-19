import { Album } from '../core/models';

export const MOCK_ALBUMS: Album[] = [
  {
    id: 'al1',
    title: 'Ethereal',
    artistId: 'a1',
    artistName: 'Luna Vega',
    cover: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&h=400&fit=crop',
    type: 'album',
    releaseDate: '2024-01-15',
    genre: 'Dream Pop',
    trackIds: ['s1', 's2'],
    totalPlays: 625000,
    description: 'A journey through neon-lit dreamscapes and ethereal soundscapes.'
  },
  {
    id: 'al2',
    title: 'Void',
    artistId: 'a2',
    artistName: 'KZMA',
    cover: 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=400&fit=crop',
    type: 'ep',
    releaseDate: '2024-03-10',
    genre: 'Trap',
    trackIds: ['s4'],
    totalPlays: 410000,
    description: 'Dark, heavy, and unapologetic. Enter the void.'
  },
  {
    id: 'al3',
    title: 'Wired',
    artistId: 'a3',
    artistName: 'The Midnight Collective',
    cover: 'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400&h=400&fit=crop',
    type: 'album',
    releaseDate: '2024-01-28',
    genre: 'Indie Rock',
    trackIds: ['s5', 's6'],
    totalPlays: 780000,
    description: 'Raw, electric, and alive. Our most ambitious record yet.'
  },
  {
    id: 'al4',
    title: 'Subsonic',
    artistId: 'a5',
    artistName: 'DRVGN',
    cover: 'https://images.unsplash.com/photo-1571330735066-03aaa9429d89?w=400&h=400&fit=crop',
    type: 'album',
    releaseDate: '2024-02-14',
    genre: 'House',
    trackIds: ['s9', 's10'],
    totalPlays: 520000,
    description: 'Feel the bass beneath your feet. A sonic experience.'
  },
  {
    id: 'al5',
    title: 'Solstice',
    artistId: 'a6',
    artistName: 'Aria Osei',
    cover: 'https://images.unsplash.com/photo-1501612780327-45045538702b?w=400&h=400&fit=crop',
    type: 'album',
    releaseDate: '2024-03-22',
    genre: 'Neo-Soul',
    trackIds: ['s11', 's12'],
    totalPlays: 1300000,
    description: 'Songs written during the longest day. Golden, warm, timeless.'
  }
];
