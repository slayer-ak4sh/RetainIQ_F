import Interaction from "../models/Interaction.js";

export const listInteractions = async (req, res) => {
  try {
    if (req.user.role === "customer" && String(req.user.customer) !== req.params.customerId) {
      return res.status(403).json({ message: "Access denied" });
    }

    const items = await Interaction.find({ customer: req.params.customerId }).sort({ occurredAt: -1 });
    res.json(items);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// GET /api/interactions — admin/employee only, across all customers
export const listAllInteractions = async (req, res) => {
  try {
    const items = await Interaction.find()
      .sort({ occurredAt: -1 })
      .populate("customer", "name location");

    // Skip interactions whose customer record no longer exists (e.g. deleted
    // before cascade-delete was in place) instead of showing "Deleted customer".
    res.json(items.filter((i) => i.customer));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

export const addInteraction = async (req, res) => {
  try {

    if (req.user.role === "customer") {
      if (!req.user.customer) {
        return res.status(400).json({ error: "No customer profile linked to your account" });
      }
      const item = await Interaction.create({
        customer: req.user.customer,
        type: "complaint",
        title: req.body.title,
        notes: req.body.notes || "",
        severity: "medium",
      });

      return res.status(201).json(item);
    }

    const item = await Interaction.create(req.body);

    return res.status(201).json(item);

  } catch (err) {
    console.error("ADD INTERACTION ERROR:", err);
    return res.status(400).json({
      error: err.message,
    });
  }
};

export const removeInteraction = async (req, res) => {
  try {
    await Interaction.findByIdAndDelete(req.params.id);
    res.status(204).end();
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};