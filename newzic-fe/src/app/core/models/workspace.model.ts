export type WorkspaceStatus = 'draft' | 'in_progress' | 'review' | 'done';
export type WorkspaceFileType = 'stem' | 'sample' | 'reference' | 'lyrics' | 'other';

export interface Workspace {
  id: string;
  title: string;
  description: string | null;
  status: WorkspaceStatus;
  ownerId: string;
  ownerName: string;
  ownerAvatar: string | null;
  collaborationId: string | null;
  publishedSongId: string | null;
  members: WorkspaceMember[];
  versionCount: number;
  commentCount: number;
  fileCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface WorkspaceMember {
  id: string;
  userId: string;
  displayName: string;
  avatar: string | null;
  role: string;
  joinedAt: string;
}

export interface WorkspaceVersion {
  id: string;
  versionNumber: number;
  audioUrl: string;
  notes: string | null;
  uploadedById: string;
  uploadedByName: string;
  uploadedByAvatar: string | null;
  duration: number;
  commentCount: number;
  createdAt: string;
}

export interface WorkspaceComment {
  id: string;
  authorId: string;
  authorName: string;
  authorAvatar: string | null;
  content: string;
  timestampSeconds: number;
  parentId: string | null;
  replies: WorkspaceComment[];
  createdAt: string;
}

export interface WorkspaceFile {
  id: string;
  name: string;
  url: string;
  fileType: WorkspaceFileType;
  sizeBytes: number;
  uploadedById: string;
  uploadedByName: string;
  createdAt: string;
}

export interface WorkspaceChatMessage {
  id: string;
  senderId: string;
  senderName: string;
  senderAvatar: string | null;
  content: string;
  createdAt: string;
}
