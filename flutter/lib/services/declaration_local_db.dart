import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/declaration_vide_model.dart';

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
      version: 1,
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
            synchronise INTEGER
          )
        ''');
      },
    );
  }

  static Future<void> ajouter(DeclarationVideModel declaration) async {
    final db = await database;
    await db.insert('declarations_vide', declaration.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  static Future<void> marquerSynchronise(String idLocal) async {
    final db = await database;
    await db.update('declarations_vide', {'synchronise': 1},
        where: 'id_local = ?', whereArgs: [idLocal]);
  }

  static Future<List<DeclarationVideModel>> getNonSynchronisees() async {
    final db = await database;
    final maps = await db.query('declarations_vide', where: 'synchronise = 0');
    return maps.map((m) => DeclarationVideModel.fromMap(m)).toList();
  }

  static Future<List<DeclarationVideModel>> getToutes() async {
    final db = await database;
    final maps = await db.query('declarations_vide', orderBy: 'date_creation DESC');
    return maps.map((m) => DeclarationVideModel.fromMap(m)).toList();
  }
}
