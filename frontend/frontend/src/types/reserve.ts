export interface Seat {
  code: string;
  status: 'AVAILABLE' | 'HELD' | 'SOLD';
}

export interface ReserveRequest {
  showId: number;
  seats: string[];
  queueTicket: string;
}

export interface Reservation {
  id: number;
  showId: number;
  seats: string[];
  totalAmount: number;
  status: 'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED';
  expiresAt: string;
}
