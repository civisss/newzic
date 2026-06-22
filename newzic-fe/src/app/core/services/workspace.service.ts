import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Workspace,
  WorkspaceMember,
  WorkspaceVersion,
  WorkspaceComment,
  WorkspaceFile,
  WorkspaceChatMessage
} from '../models';

@Injectable({ providedIn: 'root' })
export class WorkspaceService {
  private base = `${environment.apiUrl}/workspaces`;

  constructor(private http: HttpClient) {}

  // ── Workspace CRUD ──

  getMyWorkspaces(status?: string): Observable<Workspace[]> {
    const params = status ? `?status=${status}` : '';
    return this.http.get<Workspace[]>(`${this.base}${params}`);
  }

  getById(id: string): Observable<Workspace> {
    return this.http.get<Workspace>(`${this.base}/${id}`);
  }

  create(data: { title: string; description?: string; inviteUserIds?: string[] }): Observable<Workspace> {
    return this.http.post<Workspace>(this.base, data);
  }

  update(id: string, data: { title?: string; description?: string; status?: string }): Observable<Workspace> {
    return this.http.patch<Workspace>(`${this.base}/${id}`, data);
  }

  // ── Members ──

  getMembers(workspaceId: string): Observable<WorkspaceMember[]> {
    return this.http.get<WorkspaceMember[]>(`${this.base}/${workspaceId}/members`);
  }

  inviteMember(workspaceId: string, userId: string): Observable<WorkspaceMember> {
    return this.http.post<WorkspaceMember>(`${this.base}/${workspaceId}/members`, { userId });
  }

  // ── Versions ──

  getVersions(workspaceId: string): Observable<WorkspaceVersion[]> {
    return this.http.get<WorkspaceVersion[]>(`${this.base}/${workspaceId}/versions`);
  }

  uploadVersion(workspaceId: string, data: { audioUrl: string; notes?: string; duration?: number }): Observable<WorkspaceVersion> {
    return this.http.post<WorkspaceVersion>(`${this.base}/${workspaceId}/versions`, data);
  }

  // ── Comments ──

  getComments(workspaceId: string, versionId: string): Observable<WorkspaceComment[]> {
    return this.http.get<WorkspaceComment[]>(`${this.base}/${workspaceId}/versions/${versionId}/comments`);
  }

  addComment(workspaceId: string, versionId: string, data: { content: string; timestampSeconds: number; parentId?: string }): Observable<WorkspaceComment> {
    return this.http.post<WorkspaceComment>(`${this.base}/${workspaceId}/versions/${versionId}/comments`, data);
  }

  // ── Files ──

  getFiles(workspaceId: string): Observable<WorkspaceFile[]> {
    return this.http.get<WorkspaceFile[]>(`${this.base}/${workspaceId}/files`);
  }

  uploadFile(workspaceId: string, data: { name: string; url: string; fileType?: string; sizeBytes?: number }): Observable<WorkspaceFile> {
    return this.http.post<WorkspaceFile>(`${this.base}/${workspaceId}/files`, data);
  }

  deleteFile(workspaceId: string, fileId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${workspaceId}/files/${fileId}`);
  }

  // ── Chat ──

  getChatMessages(workspaceId: string, page = 0, size = 50): Observable<WorkspaceChatMessage[]> {
    return this.http.get<WorkspaceChatMessage[]>(`${this.base}/${workspaceId}/chat?page=${page}&size=${size}`);
  }

  sendChatMessage(workspaceId: string, content: string): Observable<WorkspaceChatMessage> {
    return this.http.post<WorkspaceChatMessage>(`${this.base}/${workspaceId}/chat`, { content });
  }
}
