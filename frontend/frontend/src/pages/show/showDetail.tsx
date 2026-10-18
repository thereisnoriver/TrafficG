// ShowDetail.tsx
import { useCallback, useEffect, useState } from 'react';
import styles from './showDetail.module.css';
import QueueModal from '../../components/modal/QueueModal';
import Button from '../../components/common/Button';
import type { Show } from '../../types/show';
import { fetchShow } from '../../api/show';
import { useNavigate, useParams } from 'react-router-dom';
import { useUser } from '../../contexts/UserContext';

const ShowDetail = () => {
  const {id} = useParams<{id:string}>();
  const navigate = useNavigate();
  const { user } = useUser();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [show, setShow] = useState<Show | null>(null);


  const handleReserveClick = () => {
    if (!user) {
      navigate('/login');
      return;
    }
    setIsModalOpen(true);
  };

  const handleAdmitted = useCallback((ticketId: string) => {
    if (!id) return;
    sessionStorage.setItem('queueTicket:' + id, ticketId);
    navigate('/shows/' + id + '/seatSelector');
  }, [id, navigate]);

   useEffect(() => {
    if (!id) return;
    fetchShow(Number(id))
      .then(setShow)
      .catch(console.error);
  }, [id]);

 
  if (!show) return <div>Loading...</div>;

  return (
    <div className={styles.container}>
      {show.imageUrl
        ? <img src={show.imageUrl} alt={show.name} className={styles.image} />
        : <div className={styles.image} style={{ display: 'grid', placeItems: 'center', minHeight: 170, background: '#dae9f5', color: '#24436b' }}>LIVE PERFORMANCE</div>}
      <h2 className={styles.title}>{show.name}</h2>
      <p className={styles.date}>상세정보:</p>
      <p className={styles.date}>{show.startDate} ~ {show.endDate}</p>
      <p className={styles.detail}>{show.detail}</p>
      <div className={styles.buttonMethods}>

        <Button onClick={handleReserveClick}>Join waiting room</Button>
          {isModalOpen && (
            <QueueModal showId={show.id} onClose={() => setIsModalOpen(false)} onAdmitted={handleAdmitted} />
          )}
      </div>
    </div>
  );
};

export default ShowDetail;
