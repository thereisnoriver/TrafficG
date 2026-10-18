import styles from './paymentSuccess.module.css';
import Button from '../../components/common/Button';
import { useNavigate } from 'react-router-dom';

const PaymentSuccess = () => {
  const navigate = useNavigate();

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>예매 완료!</h2>
      <p className={styles.message}>성공적으로 예매되었습니다 🎉</p>
      <p className={styles.sub}>Payment was simulated; no actual charge was made.</p>
      <Button onClick={() => navigate('/showList')}>홈으로 가기</Button>
    </div>
  );
};

export default PaymentSuccess;
