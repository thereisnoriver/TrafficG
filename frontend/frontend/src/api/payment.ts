import type { Reservation } from '../types/reserve';
import { api } from './client';

// Demo-only payment simulation. No card or payment details are collected.
export const paymentConfirm = (reservationId: number): Promise<Reservation> =>
  api<Reservation>('/api/reservations/' + reservationId + '/payment', {
    method: 'POST', body: JSON.stringify({ simulateSuccess: true }),
  });
