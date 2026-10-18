export interface User {
  id: number;
  accountId: string;
  name: string;
}

// types/auth.ts
export interface LoginRequest {
  accountId: string;
  password: string;
}


// types/SignUp.ts
export interface SignUpRequest {
  accountId: string;
  password: string;
  name: string;
  phoneNumber: string;
  address: string;
}
