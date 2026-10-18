import { api } from './client';

export interface QueueStatus {
  ticketId: string;
  state: 'WAITING' | 'ADMITTED';
  position: number;
  waitingCount: number;
}

export const joinQueue = (showId: number): Promise<QueueStatus> =>
  api<QueueStatus>('/api/queue/join', { method: 'POST', body: JSON.stringify({ showId }) });

export const getQueueStatus = (ticketId: string): Promise<QueueStatus> =>
  api<QueueStatus>('/api/queue/' + ticketId);

export const leaveQueue = (ticketId: string): Promise<void> =>
  api<void>('/api/queue/' + ticketId, { method: 'DELETE' });
