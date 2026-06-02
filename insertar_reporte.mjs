import mysql from 'file:///C:/Users/Amezc/OneDrive/Escritorio/AXF Proyecto/AXF/axf_backend/node_modules/mysql2/promise.js';

async function insertReport() {
  const db = await mysql.createConnection({
    host: '31.97.208.162',
    user: 'u544003664_axf',
    password: 'Axf_Solutions_Axel_Poncho_2006',
    database: 'u544003664_axf_gymnet'
  });

  try {
    console.log("Conectado. Insertando reporte de prueba...");
    const [result] = await db.query(`
      INSERT INTO reportes (
        id_suscriptor, id_sucursal, categoria, descripcion, 
        es_privado, estado, num_strikes, reenviado_sucursal
      ) VALUES (
        22, 1, 'Otro', 'Este es un reporte de prueba con 3 strikes para verificar el botón de reenvío.', 
        0, 'En_Proceso', 3, 0
      )
    `);
    console.log("Reporte insertado exitosamente. ID del reporte:", result.insertId);
  } catch (e) {
    console.error("Error insertando reporte:", e);
  } finally {
    await db.end();
  }
}

insertReport();
