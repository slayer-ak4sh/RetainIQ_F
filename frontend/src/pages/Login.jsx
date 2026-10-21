import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { ShieldCheck, Zap } from "lucide-react";

const DEMO_ACCOUNTS = [
  { label: "Admin", email: "admin@retainiq.com", password: "Admin@123" },
  { label: "Employee", email: "employee@retainiq.com", password: "Emp@123" },
  { label: "Customer", email: "customer@retainiq.com", password: "Cust@123" },
];


export default function Login(){

const navigate = useNavigate();

const { login } = useAuth();


const [form,setForm] = useState({
email:"",
password:""
});


const [error,setError] = useState("");
const [loading,setLoading] = useState(false);

const quickLogin = async (email, password) => {
  try {
    setLoading(true);
    setError("");
    setForm({ email, password });
    const data = await loginUser({ email, password });
    login(data);
    if (data.user.role === "admin") navigate("/admin");
    else if (data.user.role === "employee") navigate("/employee");
    else navigate("/customer");
  } catch {
    setError("Demo account not set up yet. See README.");
  } finally {
    setLoading(false);
  }
};



const update = (key) => (e) => {

setForm({
  ...form,
  [key]: e.target.value
});

};



const submit=async(e)=>{

e.preventDefault();

try{

setLoading(true);
setError("");

const data = await loginUser(form);


login(data);
if (data.user.role === "admin") {
  navigate("/admin");
} else if (data.user.role === "employee") {
  navigate("/employee");
} else if (data.user.role === "customer") {
  navigate("/customer");
} else {
  setError("Unknown user role");
}

}catch(err){

setError(
err.response?.data?.message ||
"Login failed"
);

}
finally{

setLoading(false);

}

};



return (

<div
style={{
minHeight:"100vh",
display:"flex",
alignItems:"center",
justifyContent:"center",
background:"var(--bg)"
}}
>


<form
onSubmit={submit}
className="card"
style={{
width:380,
padding:30
}}
>


<div
style={{
display:"flex",
alignItems:"center",
gap:10,
marginBottom:25
}}
>

<ShieldCheck size={30} color="var(--signal-mid)" />
<h2>Telecom Intelligence</h2>
</div>

<div style={{ background: "var(--bg)", border: "1px solid var(--border)", borderRadius: 8, padding: "12px 14px", marginBottom: 20 }}>
  <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 10 }}>
    <Zap size={14} color="var(--signal-mid)" />
    <span style={{ fontSize: 12, fontWeight: 600, color: "var(--signal-mid)", textTransform: "uppercase", letterSpacing: 0.5 }}>Demo Quick Login</span>
  </div>
  <div style={{ display: "flex", gap: 8 }}>
    {DEMO_ACCOUNTS.map((a) => (
      <button
        key={a.label}
        type="button"
        onClick={() => quickLogin(a.email, a.password)}
        disabled={loading}
        style={{ flex: 1, padding: "6px 0", fontSize: 12, fontWeight: 600, borderRadius: 6, border: "1px solid var(--border)", background: "var(--surface)", color: "var(--ink)", cursor: "pointer" }}
      >
        {a.label}
      </button>
    ))}
  </div>
  <div style={{ marginTop: 10, fontSize: 11, color: "var(--ink-muted)", lineHeight: 1.6 }}>
    <div>Admin: admin@retainiq.com / Admin@123</div>
    <div>Employee: employee@retainiq.com / Emp@123</div>
    <div>Customer: customer@retainiq.com / Cust@123</div>
  </div>
</div>



<div className="field">

<label>Email</label>

<input
type="email"
required
value={form.email}
onChange={update("email")}
/>

</div>



<div
className="field"
style={{
marginTop:15
}}
>

<label>Password</label>

<input
type="password"
required
value={form.password}
onChange={update("password")}
/>

</div>



{
error &&
<p
style={{
color:"var(--risk-high)",
marginTop:15
}}
>
{error}
</p>
}



<button
className="btn btn-primary"
style={{
width:"100%",
justifyContent:"center",
marginTop:25
}}
disabled={loading}
>

{
loading
?
"Signing in..."
:
"Login"
}

</button>


</form>


</div>

);

}