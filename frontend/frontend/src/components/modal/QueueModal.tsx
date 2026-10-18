import { useEffect, useState } from 'react';
import { getQueueStatus, joinQueue, leaveQueue } from '../../api/queue';
import type { QueueStatus } from '../../api/queue';
import styles from './QueueModal.module.css';

interface QueueModalProps {
  showId: number;
  onClose: () => void;
  onAdmitted: (ticketId: string) => void;
}

const QueueModal = ({ showId, onClose, onAdmitted }: QueueModalProps) => {
  const [status, setStatus] = useState<QueueStatus | null>(null);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    let mounted = true;
    let interval: ReturnType<typeof setInterval> | undefined;
    joinQueue(showId).then((initial) => {
      if (!mounted) return;
      setStatus(initial);
      if (initial.state === 'ADMITTED') {
        onAdmitted(initial.ticketId);
        return;
      }
      interval = setInterval(() => {
        getQueueStatus(initial.ticketId)
          .then((next) => {
            if (!mounted) return;
            setStatus(next);
            if (next.state === 'ADMITTED') {
              if (interval) clearInterval(interval);
              onAdmitted(next.ticketId);
            }
          })
          .catch((err: Error) => {
            if (mounted) setError(err.message);
            if (interval) clearInterval(interval);
          });
      }, 1500);
    }).catch((err: Error) => { if (mounted) setError(err.message); });
    return () => {
      mounted = false;
      if (interval) clearInterval(interval);
      // Keep admitted tickets when navigating to the seat selector.
    };
  }, [showId, onAdmitted]);

  const close = async () => {
    if (status?.ticketId) {
      try { await leaveQueue(status.ticketId); }
      catch { /* Expired tickets have already been removed. */ }
    }
    onClose();
  };

  return (
    <div className={styles.overlay}>
      <div className={styles.modal}>
        <h2>Waiting room</h2>
        {error && <p role="alert">{error}</p>}
        {!status && !error && <p>Joining the queue...</p>}
        {status?.state === 'WAITING' && (
          <>
            <p>People waiting: {status.waitingCount}</p>
            <p>Your position: {status.position}</p>
            <p>Your position is updated automatically.</p>
          </>
        )}
        {status?.state === 'ADMITTED' && <p>Admitted! Opening the seat selector...</p>}
        <button disabled={!status && !error} onClick={() => void close()}>Leave waiting room</button>
      </div>
    </div>
  );
};

export default QueueModal;
