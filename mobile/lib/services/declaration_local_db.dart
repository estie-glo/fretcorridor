import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/declaration_vide_model.dart';
import '../models/position_pending_model.dart';

class DeclarationLocalDb {
  static Database? _db;

  static Future<Database> get database async {
    if (_db != null) return _db!;
    _db = await _initDb();
    return _db!;
  }

  static Future<Database> _initDb() async {
    final path = join(await getDatabasesPath(), 'fretcorridor_local.db');
    return openDatabase(
      path,
      version: 3,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE declarations_vide (
            id_local TEXT PRIMARY KEY,
            axe_id TEXT,
            axe_nom TEXT,
            latitude REAL,
            longitude REAL,
            type_camion TEXT,
            capacite_tonnes REAL,
            date_creation TEXT,
            synchronise INTEGER,
            mission_id TEXT,
            disponible_de TEXT
          )
        ''');
        await _createPositionsTable(db);
      },
      onUpgrade: (db, oldVersion, newVersion) async {
        if (oldVersion < 2) {
          await db.execute(
              'ALTER TABLE declarations_vide ADD COLUMN mission_id TEXT');
          await _createPositionsTable(db);
        }
        if (oldVersion < 3) {
          await db.execute(
              'ALTER TABLE declarations_vide ADD COLUMN disponible_de TEXT');
        }
      },
    );
  }

  static Future<void> _createPositionsTable(Database db) async {
    await db.execute('''
      CREATE TABLE IF NOT EXISTS positions_pending (
        id_local TEXT PRIMARY KEY,
        mission_id TEXT NOT NULL,
        latitude REAL NOT NULL,
        longitude REAL NOT NULL,
        recorded_at TEXT NOT NULL,
        vitesse_kmh REAL,
        precision_metres REAL,
        synchronise INTEGER NOT NULL DEFAULT 0
      )
    ''');
  }

  static Future<void> ajouter(DeclarationVideModel declaration) async {
    final db = await database;
    await db.insert('declarations_vide', declaration.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  static Future<void> marquerSynchronise(String idLocal, {String? missionId}) async {
    final db = await database;
    final data = <String, Object>{'synchronise': 1};
    if (missionId != null) {
      data['mission_id'] = missionId;
    }
    await db.update('declarations_vide', data,
        where: 'id_local = ?', whereArgs: [idLocal]);
  }

  // ── Modifier une déclaration locale après édition serveur ──
  static Future<void> mettreAJour(
    String idLocal, {
    String? typeCamion,
    double? capaciteTonnes,
    DateTime? disponibleDe,
  }) async {
    final db = await database;
    final data = <String, Object>{};
    if (typeCamion != null) data['type_camion'] = typeCamion;
    if (capaciteTonnes != null) data['capacite_tonnes'] = capaciteTonnes;
    if (disponibleDe != null) data['disponible_de'] = disponibleDe.toIso8601String();
    if (data.isEmpty) return;
    await db.update('declarations_vide', data,
        where: 'id_local = ?', whereArgs: [idLocal]);
  }

  // ── Supprimer une déclaration locale ───────────────────────
  static Future<void> supprimer(String idLocal) async {
    final db = await database;
    await db.delete('declarations_vide', where: 'id_local = ?', whereArgs: [idLocal]);
  }

  static Future<String?> derniereMissionIdSynchronisee() async {
    final db = await database;
    final maps = await db.query(
      'declarations_vide',
      where: 'synchronise = 1 AND mission_id IS NOT NULL',
      orderBy: 'date_creation DESC',
      limit: 1,
    );
    if (maps.isEmpty) return null;
    return maps.first['mission_id'] as String?;
  }

  static Future<List<DeclarationVideModel>> getNonSynchronisees() async {
    final db = await database;
    final maps = await db.query('declarations_vide', where: 'synchronise = 0');
    return maps.map((m) => DeclarationVideModel.fromMap(m)).toList();
  }

  static Future<List<DeclarationVideModel>> getToutes() async {
    final db = await database;
    final maps =
        await db.query('declarations_vide', orderBy: 'date_creation DESC');
    return maps.map((m) => DeclarationVideModel.fromMap(m)).toList();
  }

  // ── Positions GPS (S5 offline) ────────────────────────────

  static Future<void> ajouterPosition(PositionPendingModel position) async {
    final db = await database;
    await db.insert('positions_pending', position.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  static Future<void> marquerPositionsSynchronisees(List<String> idLocaux) async {
    if (idLocaux.isEmpty) return;
    final db = await database;
    final placeholders = List.filled(idLocaux.length, '?').join(',');
    await db.update(
      'positions_pending',
      {'synchronise': 1},
      where: 'id_local IN ($placeholders)',
      whereArgs: idLocaux,
    );
  }

  static Future<List<PositionPendingModel>> getPositionsNonSynchronisees() async {
    final db = await database;
    final maps = await db.query('positions_pending', where: 'synchronise = 0');
    return maps.map((m) => PositionPendingModel.fromMap(m)).toList();
  }
}
