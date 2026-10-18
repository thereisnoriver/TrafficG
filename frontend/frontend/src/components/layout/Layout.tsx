// src/layout/Layout.tsx
import { Outlet } from 'react-router-dom';
import Header from './Header';
const Layout = () => {
  return (
    <>
    <Header />
    <main style={{ minHeight: 'calc(100vh - 80px)', width: '100%', margin: '0 auto', padding: '2rem' }}>
      <Outlet />
    </main>
    </> );
};

export default Layout;
