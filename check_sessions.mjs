import mysql from 'file:///C:/Users/Amezc/OneDrive/Escritorio/AXF Proyecto/AXF/axf_backend/node_modules/mysql2/promise.js';

async function updateSessions() {
  const db = await mysql.createConnection({
    host: '31.97.208.162',
    user: 'u544003664_axf',
    password: 'Axf_Solutions_Axel_Poncho_2006',
    database: 'u544003664_axf_gymnet'
  });

  try {
    const [rows] = await db.query(`
      SELECT id_suscriptor, nombres
      FROM suscriptores
      WHERE nombres LIKE '%Cristian%' OR nombres LIKE '%Alfonso%'
    `);
    
    if (rows.length > 0) {
      const id = rows[0].id_suscriptor;
      console.log('User id:', id);
      const [res] = await db.query(`
        UPDATE suscripciones 
        SET sesiones_nutriologo_restantes = 5, sesiones_entrenador_restantes = 8
        WHERE id_suscriptor = ? AND estado = 'Activa' AND fecha_fin >= CURDATE()
      `, [id]);
      console.log('Updated:', res.affectedRows);
    }
  } catch (e) {
    console.error(e);
  }

  await db.end();
}

updateSessions();
