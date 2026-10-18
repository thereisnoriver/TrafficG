import { useLocation } from 'react-router-dom';
import styles from './payment.module.css';
import Button from '../../components/common/Button';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { paymentConfirm } from '../../api/payment';
import type { Reservation } from '../../types/reserve';

const Payment = () => {
  const { state } = useLocation() as { state: { reservation?: Reservation } | null };
  const reservation = state?.reservation;
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleConfirm = async () => {
    if (!reservation) return;
    setBusy(true);
    try {
      const updated = await paymentConfirm(reservation.id);
      if (updated.status !== 'CONFIRMED') throw new Error('The hold expired before confirmation.');
      navigate('/payment/success', { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to confirm reservation');
    } finally {
      setBusy(false);
    }
  };

  if (!reservation) {
    return <p>There is no active reservation. Return to the <a href="/showList">show list</a>.</p>;
  }
  return (
    <div className={styles.container}>
      <h2 className={styles.title}>Confirm demo reservation</h2>
      <div className={styles.infoBox}>
        <p><strong>Show ID:</strong> {reservation.showId}</p>
        <p><strong>Selected seats:</strong> {reservation.seats.join(', ')}</p>
        <p><strong>Total:</strong> ₩{reservation.totalAmount.toLocaleString()}</p>
        <p><strong>Hold expires:</strong> {new Date(reservation.expiresAt).toLocaleString()}</p>
      </div>
      <p>This is a simulated payment: no card details or funds are processed.</p>
      {error && <p role="alert">{error}</p>}
      <div className={styles.confirmBox}>
        <Button type="button" disabled={busy} onClick={handleConfirm}>
          {busy ? 'Confirming...' : 'Simulate successful payment'}
        </Button>
      </div>
    </div>
  );
};

export default Payment;
