import type { ReserveRequest, Reservation, Seat } from '../types/reserve';
import { api } from './client';

export const fetchSeats = (showId: number): Promise<Seat[]> =>
  api<Seat[]>('/api/shows/' + showId + '/seats');

export const reserveSeat = (data: ReserveRequest): Promise<Reservation> =>
  api<Reservation>('/api/reservations', { method: 'POST', body: JSON.stringify(data) });
