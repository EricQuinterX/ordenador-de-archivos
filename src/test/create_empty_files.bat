@echo off

IF NOT EXIST "NEW" (mkdir NEW)
IF NOT EXIST "OLD" (mkdir OLD)

type NUL > OLD\Designer.Estructura_T_2_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_T_9_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_T_17_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_T_27_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_T_37_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_T_50_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_GPO_TR_2_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_GPO_TR_9_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_GPO_TR_17_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_GPO_TR_27_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_GPO_TR_37_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > OLD\Designer.Estructura_GRP_T_50_CategoriaGrupo.xml.Deletes.Oracle.Sql
echo "Old files created"
type NUL > NEW\CREATE_TABLE_ENT_TR_G30.sql
type NUL > NEW\CREATE_TABLE_ENT_TR_G30_LINKS.sql
type NUL > NEW\ALTER_TABLE_ENT_TR_2.sql
type NUL > NEW\ALTER_TABLE_ENT_TR_9.sql
type NUL > NEW\ALTER_TABLE_ENT_TR_17.sql
type NUL > NEW\ALTER_TABLE_ENT_TR_27.sql
type NUL > NEW\ALTER_TABLE_ENT_TR_37.sql
type NUL > NEW\ALTER_TABLE_ENT_TR_50.sql
type NUL > NEW\Designer.Estructura_T_2_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_9_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_17_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_27_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_37_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_50_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_G30_TipoTramite.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_TR_2_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_TR_9_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_TR_17_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_TR_27_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_TR_37_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_GRP_T_50_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_T_G30_CategoriaGrupo.xml.Deletes.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_2_TipoTramite.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_9_TipoTramite.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_17_TipoTramite.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_27_TipoTramite.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_37_TipoTramite.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_50_TipoTramite.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_T_G30_TipoTramite.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_TR_2_CategoriaGrupo.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_TR_9_CategoriaGrupo.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_TR_17_CategoriaGrupo.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_TR_27_CategoriaGrupo.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_TR_37_CategoriaGrupo.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_GRP_T_50_CategoriaGrupo.xml.Inserts.Oracle.Sql
type NUL > NEW\Designer.Estructura_GPO_T_G30_CategoriaGrupo.xml.Inserts.Oracle.Sql
type NUL > NEW\PACKAGE_PA_RET_IN.Sql
type NUL > NEW\PACKAGE_PA_RET_OUT.Sql
type NUL > NEW\PACKAGE_PA_RET_PSW.Sql
type NUL > NEW\PACKAGE_PA_T_50.Sql
type NUL > NEW\PACKAGE_PA_INFO_CLI.Sql
type NUL > NEW\PACKAGE_PKG_T_9.Sql
type NUL > NEW\PACKAGE_PA_GLOBAL_G36.Sql
type NUL > NEW\INS_DATOS_ENT_TR_98.Sql
type NUL > NEW\UPD_IBS_CAT_DATA.Sql
type NUL > NEW\CREO_SUPER_VISTA.Sql

echo "NEW files created"

pause