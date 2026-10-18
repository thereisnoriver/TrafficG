// src/App.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/layout/Layout';
import Home from './pages/home';
import Login from './pages/account/Login';
import Signup from './pages/account/Signup';
import ShowList from './pages/show/showList';
import ShowDetail from './pages/show/showDetail';
import SeatSelector from './pages/reserve/seatSelector';
import Payment from './pages/payment/payment';
import PaymentSuccess from './pages/payment/paymentSuccess';
import './styles/colors.css'

function App() {
  return (
    
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />

          <Route  path="login"  element={<Login />} />
          <Route path="signup" element={<Signup />} />
          
          <Route path="showList" element={<ShowList />} />
          <Route path="shows/:id" element={<ShowDetail />} />
          <Route path="shows/:id/seatSelector" element={<SeatSelector/>} />
          <Route path="payment" element={<Payment/>} />
          <Route path="payment/success" element={<PaymentSuccess />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
