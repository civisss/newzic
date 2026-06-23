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
  changelog: string[];
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
  endTimestampSeconds: number | null;
  resolved: boolean;
  resolvedByVersionNumber: number | null;
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

export type TaskStatus = 'todo' | 'in_progress' | 'done';

export interface WorkspaceTask {
  id: string;
  title: string;
  description: string | null;
  status: TaskStatus;
  assignedToId: string | null;
  assignedToName: string | null;
  assignedToAvatar: string | null;
  createdById: string;
  createdByName: string;
  timestampSeconds: number | null;
  resolvedByVersionNumber: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface WorkspaceActivity {
  id: string;
  userId: string;
  userName: string;
  userAvatar: string | null;
  type: string;
  message: string;
  createdAt: string;
}

export type ReferencePlatform = 'spotify' | 'youtube' | 'soundcloud' | 'apple_music' | 'other';

export interface WorkspaceReference {
  id: string;
  title: string;
  artist: string | null;
  url: string | null;
  notes: string | null;
  platform: ReferencePlatform;
  addedById: string;
  addedByName: string;
  createdAt: string;
}
