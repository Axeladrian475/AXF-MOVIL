import mysql from 'file:///C:/Users/Amezc/OneDrive/Escritorio/AXF Proyecto/AXF/axf_backend/node_modules/mysql2/promise.js';

async function updateDb() {
  const db = await mysql.createConnection({
    host: '193.203.166.27',
    user: 'u544003664_axf_gymn',
    password: 'Axeladrian475.',
    database: 'u544003664_axf_gymnet'
  });

  try {
    console.log("Conectado. Añadiendo columna fecha...");
    await db.query("ALTER TABLE registro_entrenamiento ADD COLUMN fecha DATE NOT NULL DEFAULT (CURRENT_DATE);");
    console.log("Columna añadida.");
  } catch (e) {
    if (e.code === 'ER_DUP_FIELDNAME') console.log("Columna fecha ya existe.");
    else console.error("Error añadiendo fecha:", e);
  }

  try {
    console.log("Añadiendo índice único...");
    await db.query("ALTER TABLE registro_entrenamiento ADD UNIQUE KEY uq_registro_serie (id_rutina_ejercicio, num_serie, fecha);");
    console.log("Índice añadido.");
  } catch (e) {
    if (e.code === 'ER_DUP_KEYNAME') console.log("Índice ya existe.");
    else console.error("Error añadiendo índice:", e);
  }

  await db.end();
}

updateDb();
