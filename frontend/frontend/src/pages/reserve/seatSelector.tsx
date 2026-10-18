import styles from './seatSelector.module.css';
import { useEffect, useState } from 'react';
import Button from '../../components/common/Button';
import { useNavigate } from 'react-router-dom';
import { fetchSeats, reserveSeat } from '../../api/reserve';
import type { Seat } from '../../types/reserve';
import { leaveQueue } from '../../api/queue';
import { useParams } from 'react-router-dom';
import { useUser } from '../../contexts/UserContext';

const SeatSelector = () => {
  const navigate = useNavigate();
  const { user } = useUser();
  const { id } = useParams<{ id: string }>();

  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const queueTicket = id ? sessionStorage.getItem('queueTicket:' + id) : null;
  const unavailable = new Set(seats.filter(seat => seat.status !== 'AVAILABLE').map(seat => seat.code));

  useEffect(() => {
    if (!id || !user || !queueTicket) return;
    fetchSeats(Number(id)).then(setSeats).catch((err: Error) => setError(err.message));
  }, [id, user, queueTicket]);

  const seatMap: (string | null)[][] = [
    ['A1', 'A2', null, 'A3', 'A4', 'A5', 'A6', null, 'A7', 'A8'],
    ['B1', 'B2', null, 'B3', 'B4', 'B5', 'B6', null, 'B7', 'B8'],
    ['C1', 'C2', null, 'C3', 'C4', 'C5', 'C6', null, 'C7', 'C8'],
    ['D1', 'D2', null, 'D3', 'D4', 'D5', 'D6', null, 'D7', 'D8'],
    ['E1', 'E2', null, 'E3', 'E4', 'E5', 'E6', null, 'E7', 'E8'],
    ['F1', 'F2', null, 'F3', 'F4', 'F5', 'F6', null, 'F7', 'F8'],
  ];

const handleSelect = (seatId: string) => {
  if (unavailable.has(seatId)) return;
  if (selectedSeats.includes(seatId)) {
    setSelectedSeats(prev => prev.filter(id => id !== seatId));
    return;
  }

  if (selectedSeats.length >= 4) {
    alert('최대 4개까지 선택 가능합니다.');
    return;
  }

  setSelectedSeats(prev => [...prev, seatId]);
};

const handlePayment = async () => {
  if (!queueTicket || !user || !id || selectedSeats.length === 0) return;
  setBusy(true);
  try {
    const result = await reserveSeat({
      showId: Number(id),
      seats: selectedSeats,
      queueTicket,
    });
    sessionStorage.removeItem('queueTicket:' + id);
    navigate('/payment', { state: { reservation: result } });
  } catch (err) {
    setError(err instanceof Error ? err.message : 'Reservation failed');
    fetchSeats(Number(id)).then(setSeats).catch(() => {});
  } finally {
    setBusy(false);
  }
};

  const handleCancel = async () => {
    if (queueTicket && id) {
      try { await leaveQueue(queueTicket); }
      catch { /* An expired ticket is already released. */ }
      sessionStorage.removeItem('queueTicket:' + id);
      navigate('/shows/' + id);
    }
  };

  if (!user) return <p>Please <a href="/login">sign in</a> to reserve seats.</p>;
  if (!queueTicket) return <p>Join the waiting room from the show page before choosing seats.</p>;

  return (
    <div className={styles.container}>
      <div className={styles.screen}>SCREEN</div>
      {error && <p role="alert">{error}</p>}
        <div className={styles.grid}>
          {seatMap.map((row, rowIdx) =>
            row.map((seatId, colIdx) =>
              seatId ? (
                <button
                  key={seatId}
                  className={`${styles.seat} ${
                    unavailable.has(seatId)
                      ? styles.reserved
                      : selectedSeats.includes(seatId)
                      ? styles.selected
                      : ''
                  }`}
                  onClick={() => handleSelect(seatId)}
                  disabled={unavailable.has(seatId) || busy || seats.length === 0}
                >
                  {seatId}
                </button>
              ) : (
                <div key={`empty-${rowIdx}-${colIdx}`} className={styles.empty}></div>
              )
            )
          )}
      </div>
      <div className={styles.paymentBox}>
        <p>선택한 좌석: {selectedSeats.join(', ')}</p>
      </div>
      <p>Each seat costs ₩6,000. Seats are held for five minutes after reservation.</p>
      <Button disabled={busy || selectedSeats.length === 0 || seats.length === 0} onClick={handlePayment}>
        {busy ? 'Reserving...' : 'Hold selected seats'}
      </Button>
      <Button type="button" disabled={busy} onClick={() => void handleCancel()}>Release admission and go back</Button>

    </div>
  );
};

export default SeatSelector;
