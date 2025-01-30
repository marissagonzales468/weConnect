postgresql://neondb_owner:npg_qTtn1OZLf0Pe@ep-tight-firefly-a6ora2ry.us-west-2.aws.neon.tech/neondb?sslmode=require
import type { Express } from "express";
import { createServer, type Server } from "http";
import { db } from "@db";
import { services, outreachTasks, events, statistics, cases, volunteers, volunteer_hours } from "@db/schema";
import { eq } from "drizzle-orm";

export function registerRoutes(app: Express): Server {
  const httpServer = createServer(app);

  // Services endpoints
  app.get("/api/services", async (_req, res) => {
    const allServices = await db.select().from(services);
    res.json(allServices);
  });

  app.get("/api/services/:id", async (req, res) => {
    const service = await db.select().from(services).where(eq(services.id, parseInt(req.params.id)));
    res.json(service[0]);
  });

  // Bed availability endpoint
  app.patch("/api/services/:id/beds", async (req, res) => {
    const { available_beds } = req.body;
    if (typeof available_beds !== 'number' || available_beds < 0) {
      return res.status(400).json({ message: "Invalid bed count" });
    }

    const updated = await db
      .update(services)
      .set({ available_beds })
      .where(eq(services.id, parseInt(req.params.id)))
      .returning();

    res.json(updated[0]);
  });

  // Case management endpoints
  app.get("/api/cases", async (_req, res) => {
    const allCases = await db.select().from(cases);
    res.json(allCases);
  });

  app.post("/api/cases", async (req, res) => {
    const newCase = await db.insert(cases).values(req.body).returning();
    res.json(newCase[0]);
  });

  app.patch("/api/cases/:id", async (req, res) => {
    const updated = await db
      .update(cases)
      .set({ ...req.body, updated_at: new Date() })
      .where(eq(cases.id, parseInt(req.params.id)))
      .returning();
    res.json(updated[0]);
  });

  // Volunteer management endpoints
  app.get("/api/volunteers", async (_req, res) => {
    const allVolunteers = await db.select().from(volunteers);
    res.json(allVolunteers);
  });

  app.post("/api/volunteers", async (req, res) => {
    const newVolunteer = await db.insert(volunteers).values(req.body).returning();
    res.json(newVolunteer[0]);
  });

  app.patch("/api/volunteers/:id", async (req, res) => {
    const updated = await db
      .update(volunteers)
      .set(req.body)
      .where(eq(volunteers.id, parseInt(req.params.id)))
      .returning();
    res.json(updated[0]);
  });

  // Volunteer hours tracking
  app.get("/api/volunteer-hours", async (_req, res) => {
    const hours = await db.select().from(volunteer_hours);
    res.json(hours);
  });

  app.post("/api/volunteer-hours", async (req, res) => {
    const hours = await db.insert(volunteer_hours).values(req.body).returning();
    res.json(hours[0]);
  });

  // Outreach tasks endpoints
  app.get("/api/tasks", async (_req, res) => {
    const tasks = await db.select().from(outreachTasks);
    res.json(tasks);
  });

  app.post("/api/tasks", async (req, res) => {
    const task = await db.insert(outreachTasks).values(req.body).returning();
    res.json(task[0]);
  });

  // Events endpoints
  app.get("/api/events", async (_req, res) => {
    const allEvents = await db.select().from(events);
    res.json(allEvents);
  });

  // Statistics endpoints
  app.get("/api/statistics", async (_req, res) => {
    const stats = await db.select().from(statistics);
    res.json(stats);
  });

  return httpServer;
}