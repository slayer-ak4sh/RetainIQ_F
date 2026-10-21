import mongoose from "mongoose";
import bcrypt from "bcrypt";
import dotenv from "dotenv";
import User from "./models/User.js";
import Customer from "./models/Customer.js";

dotenv.config();

const DEMO_USERS = [
  { name: "Admin Demo", email: "admin@retainiq.com", password: "Admin@123", role: "admin" },
  { name: "Employee Demo", email: "employee@retainiq.com", password: "Emp@123", role: "employee" },
  { name: "Customer Demo", email: "customer@retainiq.com", password: "Cust@123", role: "customer" },
];

async function seed() {
  const uri = process.env.MONGO_URI ||
    `mongodb://${process.env.MONGO_USER || "admin"}:${process.env.MONGO_PASSWORD || "Admin@123"}@localhost:27017/churnDB?authSource=admin`;
  await mongoose.connect(uri);
  console.log("MongoDB connected");

  for (const u of DEMO_USERS) {
    const existing = await User.findOne({ email: u.email });
    if (existing) { console.log(`Already exists: ${u.email}`); continue; }

    const hashed = await bcrypt.hash(u.password, 10);
    const user = await User.create({ ...u, password: hashed });

    if (u.role === "customer") {
      const customer = await Customer.create({
        name: u.name,
        email: u.email,
        phone: "9999999999",
        location: "Mumbai",
        region: "West",
        tenure: 24,
        monthlyCharges: 65,
        totalCharges: 1560,
        contract: "One year",
        internetService: "Fiber optic",
        paymentMethod: "Bank transfer (automatic)",
        techSupport: "Yes",
        onlineSecurity: "Yes",
        churnProbability: 0.12,
      });
      user.customer = customer._id;
      await user.save();
    }

    console.log(`Created [${u.role}] → ${u.email} / ${u.password}`);
  }

  process.exit(0);
}

seed().catch((err) => { console.error(err); process.exit(1); });
