import styles from './Login.module.css';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';
import ButtonWrapper from '../../components/common/ButtonWrapper';
import LinkButton from '../../components/common/LinkButton';
import { useState } from 'react';
import { login } from '../../api/auth'; // 아까 만든 login 함수
import { useNavigate } from 'react-router-dom';
import { useUser } from '../../contexts/UserContext';

const Login = () => {
  const [accountId, setAccountId] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();
  const { setUser } = useUser();
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault(); // 새로고침 막기

    try {
      const user = await login({ accountId, password });
      setUser(user);
      navigate('/showList');
      // 여기에 로그인 성공 후 로직 (예: 페이지 이동, 상태 저장 등)
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Login failed');
    }
  };

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>LOGIN</h2>
      <form className={styles.form} onSubmit={handleLogin}>
        <Input type="text" placeholder="ID" value={accountId}  onChange={(e) => setAccountId(e.target.value)} />
        <Input type="password" placeholder="PW" value={password} onChange={(e) => setPassword(e.target.value)} />
        <ButtonWrapper>
          <Button type="submit">LOGIN</Button>
          <LinkButton to="/signup">SignUp</LinkButton>
        </ButtonWrapper>
      </form>
    </div>
  );
};

export default Login;
