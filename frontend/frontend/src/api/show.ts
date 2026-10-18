import type { Show } from '../types/show';
import { api } from './client';

export const fetchShows = (): Promise<Show[]> => api<Show[]>('/api/shows');
export const fetchShow = (id: number): Promise<Show> => api<Show>('/api/shows/' + id);
