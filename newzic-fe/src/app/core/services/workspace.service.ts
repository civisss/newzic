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
  WorkspaceChatMessage,
  WorkspaceTask,
  WorkspaceActivity,
  WorkspaceReference
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

  uploadVersion(workspaceId: string, data: {
    audioUrl: string; notes?: string; changelog?: string[];
    duration?: number; resolveCommentIds?: string[]; resolveTaskIds?: string[]
  }): Observable<WorkspaceVersion> {
    return this.http.post<WorkspaceVersion>(`${this.base}/${workspaceId}/versions`, data);
  }

  // ── Comments ──

  getComments(workspaceId: string, versionId: string): Observable<WorkspaceComment[]> {
    return this.http.get<WorkspaceComment[]>(`${this.base}/${workspaceId}/versions/${versionId}/comments`);
  }

  addComment(workspaceId: string, versionId: string, data: {
    content: string; timestampSeconds: number; endTimestampSeconds?: number; parentId?: string
  }): Observable<WorkspaceComment> {
    return this.http.post<WorkspaceComment>(`${this.base}/${workspaceId}/versions/${versionId}/comments`, data);
  }

  resolveComment(workspaceId: string, versionId: string, commentId: string, resolved: boolean): Observable<WorkspaceComment> {
    return this.http.patch<WorkspaceComment>(
      `${this.base}/${workspaceId}/versions/${versionId}/comments/${commentId}/resolve`,
      { resolved }
    );
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

  // ── Tasks ──

  getTasks(workspaceId: string): Observable<WorkspaceTask[]> {
    return this.http.get<WorkspaceTask[]>(`${this.base}/${workspaceId}/tasks`);
  }

  createTask(workspaceId: string, data: {
    title: string; description?: string; assignedToId?: string; timestampSeconds?: number
  }): Observable<WorkspaceTask> {
    return this.http.post<WorkspaceTask>(`${this.base}/${workspaceId}/tasks`, data);
  }

  updateTask(workspaceId: string, taskId: string, data: {
    title?: string; description?: string; status?: string; assignedToId?: string
  }): Observable<WorkspaceTask> {
    return this.http.patch<WorkspaceTask>(`${this.base}/${workspaceId}/tasks/${taskId}`, data);
  }

  deleteTask(workspaceId: string, taskId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${workspaceId}/tasks/${taskId}`);
  }

  // ── Activities ──

  getActivities(workspaceId: string, page = 0, size = 50): Observable<WorkspaceActivity[]> {
    return this.http.get<WorkspaceActivity[]>(`${this.base}/${workspaceId}/activities?page=${page}&size=${size}`);
  }

  // ── References ──

  getReferences(workspaceId: string): Observable<WorkspaceReference[]> {
    return this.http.get<WorkspaceReference[]>(`${this.base}/${workspaceId}/references`);
  }

  addReference(workspaceId: string, data: {
    title: string; artist?: string; url?: string; notes?: string; platform?: string
  }): Observable<WorkspaceReference> {
    return this.http.post<WorkspaceReference>(`${this.base}/${workspaceId}/references`, data);
  }

  deleteReference(workspaceId: string, refId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${workspaceId}/references/${refId}`);
  }
}
