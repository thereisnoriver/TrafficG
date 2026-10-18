// Entry point for the TrafficG reservation demo.

import styles from './home.module.css';
import { Link } from 'react-router-dom';

const Home = () => {
  return (
    <div className={styles.container}>
        <div className={styles.hero}>
            <h1 className={styles.title}>TrafficG</h1>
            <p className={styles.subtitle}>Concert reservations with an admission queue and transactional seat holds.</p>
            <Link to="/showList" className={styles.cta}>Explore performances</Link>
        </div>
    </div>

  );
};

export default Home;
