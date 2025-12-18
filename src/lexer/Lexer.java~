
package lexer;

import java_cup.runtime.*;
import parser.sym;
import reports.TablaErrores;
import reports.ErrorTipo;



@SuppressWarnings("fallthrough")
public class Lexer implements java_cup.runtime.Scanner {


  public static final int YYEOF = -1;


  private static final int ZZ_BUFFERSIZE = 16384;


  public static final int YYINITIAL = 0;

  
  private static final int ZZ_LEXSTATE[] = {
     0, 0
  };

 
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\1\u0100\37\u0200\1\u0300\u10de\u0200";

  private static int [] zzUnpackcmap_top() {
    int [] result = new int[4352];
    int offset = 0;
    offset = zzUnpackcmap_top(ZZ_CMAP_TOP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_top(String packed, int offset, int [] result) {
    int i = 0;       
    int j = offset;  
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  
  private static final int [] ZZ_CMAP_BLOCKS = zzUnpackcmap_blocks();

  private static final String ZZ_CMAP_BLOCKS_PACKED_0 =
    "\11\0\1\1\1\2\1\0\1\1\1\3\22\0\1\1"+
    "\1\4\1\5\2\0\1\6\1\7\1\10\1\11\1\12"+
    "\1\13\1\14\1\15\1\16\1\17\1\20\1\21\11\22"+
    "\1\23\1\24\1\25\1\26\1\27\2\0\1\30\1\31"+
    "\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41"+
    "\1\42\1\43\1\41\1\44\1\45\1\46\1\41\1\47"+
    "\1\50\1\51\1\52\1\53\1\54\3\41\1\0\1\55"+
    "\1\0\1\56\1\41\1\0\1\30\1\31\1\32\1\33"+
    "\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43"+
    "\1\41\1\57\1\45\1\46\1\41\1\60\1\50\1\61"+
    "\1\52\1\53\1\54\3\41\1\62\1\63\1\64\262\0"+
    "\2\65\115\0\1\66\u01aa\0\1\67\325\0";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[1024];
    int offset = 0;
    offset = zzUnpackcmap_blocks(ZZ_CMAP_BLOCKS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_blocks(String packed, int offset, int [] result) {
    int i = 0;       
    int j = offset;  
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\2\2\1\3\1\1\1\4\2\1\1\5"+
    "\1\6\1\7\1\10\1\11\1\12\1\13\2\14\1\15"+
    "\1\16\1\17\1\20\1\21\14\22\1\23\1\24\1\1"+
    "\1\25\2\1\1\26\1\0\1\27\1\0\1\30\2\0"+
    "\1\31\1\0\1\2\1\0\1\32\1\33\1\34\6\22"+
    "\1\35\3\22\1\36\7\22\1\37\1\36\3\0\1\40"+
    "\1\0\1\41\3\22\1\0\5\22\1\0\1\22\1\42"+
    "\1\43\1\22\1\0\2\22\1\0\1\22\1\44\1\22"+
    "\1\0\1\43\1\0\1\45\1\22\2\46\1\47\3\22"+
    "\2\50\1\0\1\22\1\0\1\22\1\0\1\22\1\0"+
    "\1\51\1\22\1\0\2\52\1\22\1\0\2\22\1\51"+
    "\2\53\1\22\1\0\1\22\1\0\2\54\1\22\1\0"+
    "\1\22\1\55\1\22\1\0\2\56\2\57\1\22\1\0"+
    "\1\60\2\53\2\61";

  private static int [] zzUnpackAction() {
    int [] result = new int[157];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       
    int j = offset;  
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\70\0\70\0\160\0\250\0\340\0\70\0\u0118"+
    "\0\u0150\0\70\0\70\0\u0188\0\70\0\70\0\70\0\u01c0"+
    "\0\u01f8\0\u0230\0\70\0\70\0\u0268\0\u02a0\0\u02d8\0\u0310"+
    "\0\u0348\0\u0380\0\u03b8\0\u03f0\0\u0428\0\u0460\0\u0498\0\u04d0"+
    "\0\u0508\0\u0540\0\u0578\0\70\0\70\0\u05b0\0\70\0\u05e8"+
    "\0\u0620\0\70\0\340\0\70\0\u0658\0\70\0\u0690\0\u06c8"+
    "\0\70\0\u0700\0\u0738\0\u0770\0\70\0\70\0\70\0\u07a8"+
    "\0\u07e0\0\u0818\0\u0850\0\u0888\0\u08c0\0\u08f8\0\u0930\0\u0968"+
    "\0\u09a0\0\u0310\0\u09d8\0\u0a10\0\u0a48\0\u0a80\0\u0ab8\0\u0af0"+
    "\0\u0b28\0\70\0\70\0\u0b60\0\u0b98\0\u0bd0\0\70\0\u0c08"+
    "\0\u0770\0\u0c40\0\u0c78\0\u0cb0\0\u0ce8\0\u0d20\0\u0d58\0\u0d90"+
    "\0\u0dc8\0\u0e00\0\u0e38\0\u0e70\0\u0310\0\u0310\0\u0ea8\0\u0ee0"+
    "\0\u0f18\0\u0f50\0\u0f88\0\u0fc0\0\u0310\0\u0ff8\0\u1030\0\70"+
    "\0\u1068\0\u0310\0\u10a0\0\u0310\0\70\0\u0310\0\u10d8\0\u1110"+
    "\0\u1148\0\u0310\0\70\0\u1180\0\u11b8\0\u11f0\0\u1228\0\u1260"+
    "\0\u1298\0\u12d0\0\u0310\0\u1308\0\u1340\0\u0310\0\70\0\u1378"+
    "\0\u13b0\0\u13e8\0\u1420\0\70\0\u1458\0\u1490\0\u14c8\0\u1500"+
    "\0\u1538\0\u1570\0\u0310\0\70\0\u15a8\0\u15e0\0\u1618\0\u0310"+
    "\0\u1650\0\u1688\0\u0310\0\70\0\u0310\0\70\0\u16c0\0\u16f8"+
    "\0\u0310\0\u0310\0\70\0\u0310\0\70";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[157];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  
    int j = offset; 
    int l = packed.length() - 1;
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  
  private static final int [] ZZ_TRANS = zzUnpacktrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\2\3\1\4\1\5\1\6\1\7\1\10\1\11"+
    "\1\12\1\13\1\14\1\15\1\16\1\17\1\2\1\20"+
    "\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30"+
    "\1\31\1\32\1\33\1\34\1\35\2\30\1\36\5\30"+
    "\1\37\1\30\1\40\1\41\1\30\1\42\1\43\1\2"+
    "\1\44\2\30\1\41\1\45\1\46\1\47\1\50\1\51"+
    "\1\2\72\0\1\3\113\0\1\52\41\0\5\53\1\54"+
    "\47\53\1\55\12\53\7\0\1\56\60\0\10\57\1\0"+
    "\44\57\1\60\12\57\13\0\1\61\67\0\1\62\4\0"+
    "\1\63\66\0\1\64\67\0\1\64\1\0\2\22\73\0"+
    "\1\65\67\0\1\66\67\0\1\67\62\0\2\30\5\0"+
    "\25\30\2\0\3\30\27\0\2\30\5\0\15\30\1\70"+
    "\1\30\1\71\5\30\2\0\1\30\1\71\1\30\27\0"+
    "\2\30\5\0\1\72\6\30\1\73\5\30\1\74\7\30"+
    "\2\0\3\30\27\0\2\30\5\0\4\30\1\75\10\30"+
    "\1\76\7\30\2\0\3\30\27\0\2\30\5\0\13\30"+
    "\1\77\11\30\2\0\3\30\27\0\2\30\5\0\1\100"+
    "\14\30\1\101\7\30\2\0\3\30\27\0\2\30\5\0"+
    "\5\30\1\102\6\30\1\103\10\30\2\0\1\103\2\30"+
    "\27\0\2\30\5\0\17\30\1\104\5\30\2\0\1\30"+
    "\1\104\1\30\27\0\2\30\5\0\21\30\1\105\2\30"+
    "\1\106\2\0\2\30\1\105\27\0\2\30\5\0\17\30"+
    "\1\107\5\30\2\0\1\30\1\107\1\30\27\0\2\30"+
    "\5\0\1\110\24\30\2\0\3\30\27\0\2\30\5\0"+
    "\7\30\1\111\15\30\2\0\3\30\71\0\1\112\41\0"+
    "\1\113\6\0\1\114\12\0\1\114\61\0\1\115\2\0"+
    "\1\116\4\0\1\115\13\0\1\53\2\0\1\53\44\0"+
    "\1\53\1\0\3\53\16\0\1\117\64\0\1\57\2\0"+
    "\1\57\44\0\1\57\1\0\3\57\6\0\13\62\1\120"+
    "\54\62\2\63\2\0\64\63\21\0\2\121\66\0\2\30"+
    "\5\0\15\30\1\122\7\30\2\0\3\30\27\0\2\30"+
    "\5\0\4\30\1\123\20\30\2\0\3\30\27\0\2\30"+
    "\5\0\20\30\1\124\4\30\2\0\3\30\4\0\1\125"+
    "\22\0\2\30\5\0\1\126\24\30\2\0\3\30\27\0"+
    "\2\30\5\0\14\30\1\127\10\30\2\0\1\127\2\30"+
    "\27\0\2\30\5\0\5\30\1\130\17\30\2\0\3\30"+
    "\27\0\2\30\5\0\22\30\1\131\2\30\2\0\3\30"+
    "\27\0\2\30\5\0\20\30\1\132\4\30\2\0\3\30"+
    "\4\0\1\133\22\0\2\30\5\0\13\30\1\134\11\30"+
    "\2\0\3\30\27\0\2\30\5\0\17\30\1\135\5\30"+
    "\2\0\1\30\1\135\1\30\27\0\2\30\5\0\21\30"+
    "\1\136\3\30\2\0\2\30\1\136\27\0\2\30\5\0"+
    "\10\30\1\137\14\30\2\0\3\30\3\0\1\140\23\0"+
    "\2\30\5\0\17\30\1\141\5\30\2\0\1\30\1\141"+
    "\1\30\27\0\2\30\5\0\10\30\1\142\14\30\2\0"+
    "\3\30\3\0\1\143\23\0\2\30\5\0\22\30\1\144"+
    "\2\30\2\0\3\30\27\0\2\30\5\0\17\30\1\145"+
    "\5\30\2\0\1\30\1\145\1\30\27\0\2\30\5\0"+
    "\10\30\1\146\14\30\2\0\3\30\3\0\1\147\53\0"+
    "\1\150\7\0\1\150\55\0\1\151\10\0\1\151\47\0"+
    "\1\143\24\0\1\143\2\0\13\62\1\120\4\62\1\3"+
    "\47\62\21\0\2\30\5\0\13\30\1\152\11\30\2\0"+
    "\3\30\27\0\2\30\5\0\1\153\24\30\2\0\3\30"+
    "\27\0\2\30\5\0\4\30\1\154\20\30\2\0\3\30"+
    "\42\0\1\155\54\0\2\30\5\0\17\30\1\156\5\30"+
    "\2\0\1\30\1\156\1\30\27\0\2\30\5\0\21\30"+
    "\1\157\3\30\2\0\2\30\1\157\27\0\2\30\5\0"+
    "\1\160\24\30\2\0\3\30\27\0\2\30\5\0\1\30"+
    "\1\161\23\30\2\0\3\30\27\0\2\30\5\0\4\30"+
    "\1\162\20\30\2\0\3\30\42\0\1\163\54\0\2\30"+
    "\5\0\20\30\1\144\4\30\2\0\3\30\4\0\1\164"+
    "\22\0\2\30\5\0\14\30\1\165\10\30\2\0\1\165"+
    "\2\30\52\0\1\166\12\0\1\166\31\0\2\30\5\0"+
    "\10\30\1\167\14\30\2\0\3\30\3\0\1\170\23\0"+
    "\2\30\5\0\21\30\1\171\3\30\2\0\2\30\1\171"+
    "\57\0\1\172\7\0\1\172\27\0\2\30\5\0\4\30"+
    "\1\173\20\30\2\0\3\30\27\0\2\30\5\0\13\30"+
    "\1\174\11\30\2\0\3\30\51\0\1\175\64\0\1\170"+
    "\24\0\1\170\23\0\2\30\5\0\12\30\1\176\12\30"+
    "\2\0\3\30\5\0\1\177\21\0\2\30\5\0\10\30"+
    "\1\200\14\30\2\0\3\30\3\0\1\201\23\0\2\30"+
    "\5\0\22\30\1\202\2\30\2\0\3\30\27\0\2\30"+
    "\5\0\13\30\1\203\11\30\2\0\3\30\42\0\1\204"+
    "\54\0\2\30\5\0\21\30\1\205\3\30\2\0\2\30"+
    "\1\205\57\0\1\206\7\0\1\206\27\0\2\30\5\0"+
    "\14\30\1\207\10\30\2\0\1\207\2\30\52\0\1\210"+
    "\12\0\1\210\31\0\2\30\5\0\2\30\1\211\22\30"+
    "\2\0\3\30\40\0\1\212\56\0\2\30\5\0\4\30"+
    "\1\213\20\30\2\0\3\30\42\0\1\214\54\0\2\30"+
    "\5\0\14\30\1\215\10\30\2\0\1\215\2\30\52\0"+
    "\1\216\12\0\1\216\31\0\2\30\5\0\13\30\1\217"+
    "\11\30\2\0\3\30\27\0\2\30\5\0\4\30\1\220"+
    "\20\30\2\0\3\30\27\0\2\30\5\0\13\30\1\221"+
    "\11\30\2\0\3\30\51\0\1\222\45\0\2\30\5\0"+
    "\6\30\1\223\16\30\2\0\3\30\44\0\1\224\52\0"+
    "\2\30\5\0\7\30\1\225\15\30\2\0\3\30\45\0"+
    "\1\226\51\0\2\30\5\0\22\30\1\227\2\30\2\0"+
    "\3\30\60\0\1\230\36\0\2\30\5\0\21\30\1\231"+
    "\3\30\2\0\2\30\1\231\27\0\2\30\5\0\14\30"+
    "\1\232\10\30\2\0\1\232\2\30\52\0\1\233\12\0"+
    "\1\233\31\0\2\30\5\0\4\30\1\234\20\30\2\0"+
    "\3\30\42\0\1\235\33\0";

  private static int [] zzUnpacktrans() {
    int [] result = new int[5936];
    int offset = 0;
    offset = zzUnpacktrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpacktrans(String packed, int offset, int [] result) {
    int i = 0;       
    int j = offset;  
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }



  private static final int ZZ_UNKNOWN_ERROR = 0;

  private static final int ZZ_NO_MATCH = 1;

  private static final int ZZ_PUSHBACK_2BIG = 2;

  /**
   * Error messages for {@link #ZZ_UNKNOWN_ERROR}, {@link #ZZ_NO_MATCH}, and
   * {@link #ZZ_PUSHBACK_2BIG} respectively.
   */
  private static final String ZZ_ERROR_MSG[] = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state {@code aState}
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\2\11\3\1\1\11\2\1\2\11\1\1\3\11"+
    "\3\1\2\11\17\1\2\11\1\1\1\11\2\1\1\11"+
    "\1\0\1\11\1\0\1\11\2\0\1\11\1\0\1\1"+
    "\1\0\3\11\22\1\2\11\3\0\1\11\1\0\4\1"+
    "\1\0\5\1\1\0\4\1\1\0\2\1\1\0\3\1"+
    "\1\0\1\11\1\0\3\1\1\11\5\1\1\11\1\0"+
    "\1\1\1\0\1\1\1\0\1\1\1\0\2\1\1\0"+
    "\1\1\1\11\1\1\1\0\2\1\1\11\3\1\1\0"+
    "\1\1\1\0\1\1\1\11\1\1\1\0\3\1\1\0"+
    "\1\1\1\11\1\1\1\11\1\1\1\0\2\1\1\11"+
    "\1\1\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[157];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       
    int j = offset;
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  private java.io.Reader zzReader;


  private int zzState;


  private int zzLexicalState = YYINITIAL;

  
  private char zzBuffer[] = new char[Math.min(ZZ_BUFFERSIZE, zzMaxBufferLen())];


  private int zzMarkedPos;


  private int zzCurrentPos;

  /** Marks the beginning of the {@link #yytext()} string in the buffer. */
  private int zzStartRead;


  private int zzEndRead;

  /**
   * Whether the scanner is at the end of file.
   * @see #yyatEOF
   */
  private boolean zzAtEOF;

  /**
   * The number of occupied positions in {@link #zzBuffer} beyond {@link #zzEndRead}.
   *
   * <p>When a lead/high surrogate has been read from the input stream into the final
   * {@link #zzBuffer} position, this will have a value of 1; otherwise, it will have a value of 0.
   */
  private int zzFinalHighSurrogate = 0;

  /** Number of newlines encountered up to the start of the matched text. */
  private int yyline;

  /** Number of characters from the last newline up to the start of the matched text. */
  private int yycolumn;

  /** Number of characters up to the start of the matched text. */
  @SuppressWarnings("unused")
  private long yychar;

  /** Whether the scanner is currently at the beginning of a line. */
  @SuppressWarnings("unused")
  private boolean zzAtBOL = true;

  /** Whether the user-EOF-code has already been executed. */
  private boolean zzEOFDone;

  /* user code: */
  private TablaErrores tablaErrores;

public void setTablaErrores(TablaErrores tablaErrores) {
    this.tablaErrores = tablaErrores;
}

    private Symbol symbol(int type) {
        return new Symbol(type, yyline+1, yycolumn+1);
    }
    
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline+1, yycolumn+1, value);
    }

    // Helper method to unescape strings
    private String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
                if (i < s.length()) {
                    char next = s.charAt(i);
                    switch (next) {
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case '\\': sb.append('\\'); break;
                        case '\"': sb.append('\"'); break;
                        case '\'': sb.append('\''); break;
                        default: sb.append(next); break;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public Lexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** Returns the maximum size of the scanner buffer, which limits the size of tokens. */
  private int zzMaxBufferLen() {
    return Integer.MAX_VALUE;
  }

  /**  Whether the scanner buffer can grow to accommodate a larger token. */
  private boolean zzCanGrow() {
    return true;
  }

  /**
   * Translates raw input code points to DFA table row
   */
  private static int zzCMap(int input) {
    int offset = input & 255;
    return offset == input ? ZZ_CMAP_BLOCKS[offset] : ZZ_CMAP_BLOCKS[ZZ_CMAP_TOP[input >> 8] | offset];
  }

  /**
   * Refills the input buffer.
   *
   * @return {@code false} iff there was new input.
   * @exception java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead - zzStartRead);

      /* translate stored positions */
      zzEndRead -= zzStartRead;
      zzCurrentPos -= zzStartRead;
      zzMarkedPos -= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length - zzFinalHighSurrogate && zzCanGrow()) {
      /* if not, and it can grow: blow it up */
      char newBuffer[] = new char[Math.min(zzBuffer.length * 2, zzMaxBufferLen())];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
    }

    /* fill the buffer with new input */
    int requested = zzBuffer.length - zzEndRead;
    int numRead = zzReader.read(zzBuffer, zzEndRead, requested);

    /* not supposed to occur according to specification of java.io.Reader */
    if (numRead == 0) {
      if (requested == 0) {
        throw new java.io.EOFException("Scan buffer limit reached ["+zzBuffer.length+"]");
      }
      else {
        throw new java.io.IOException(
            "Reader returned 0 characters. See JFlex examples/zero-reader for a workaround.");
      }
    }
    if (numRead > 0) {
      zzEndRead += numRead;
      if (Character.isHighSurrogate(zzBuffer[zzEndRead - 1])) {
        if (numRead == requested) { // We requested too few chars to encode a full Unicode character
          --zzEndRead;
          zzFinalHighSurrogate = 1;
        } else {                    // There is room in the buffer for at least one more char
          int c = zzReader.read();  // Expecting to read a paired low surrogate char
          if (c == -1) {
            return true;
          } else {
            zzBuffer[zzEndRead++] = (char)c;
          }
        }
      }
      /* potentially more input available */
      return false;
    }

    /* numRead < 0 ==> end of stream */
    return true;
  }


  /**
   * Closes the input reader.
   *
   * @throws java.io.IOException if the reader could not be closed.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true; // indicate end of file
    zzEndRead = zzStartRead; // invalidate buffer

    if (zzReader != null) {
      zzReader.close();
    }
  }


  /**
   * Resets the scanner to read from a new input stream.
   *
   * <p>Does not close the old reader.
   *
   * <p>All internal variables are reset, the old input stream <b>cannot</b> be reused (internal
   * buffer is discarded and lost). Lexical state is set to {@code ZZ_INITIAL}.
   *
   * <p>Internal scan buffer is resized down to its initial length, if it has grown.
   *
   * @param reader The new input stream.
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzEOFDone = false;
    yyResetPosition();
    zzLexicalState = YYINITIAL;
    int initBufferSize = Math.min(ZZ_BUFFERSIZE, zzMaxBufferLen());
    if (zzBuffer.length > initBufferSize) {
      zzBuffer = new char[initBufferSize];
    }
  }

  /**
   * Resets the input position.
   */
  private final void yyResetPosition() {
      zzAtBOL  = true;
      zzAtEOF  = false;
      zzCurrentPos = 0;
      zzMarkedPos = 0;
      zzStartRead = 0;
      zzEndRead = 0;
      zzFinalHighSurrogate = 0;
      yyline = 0;
      yycolumn = 0;
      yychar = 0L;
  }


  /**
   * Returns whether the scanner has reached the end of the reader it reads from.
   *
   * @return whether the scanner has reached EOF.
   */
  public final boolean yyatEOF() {
    return zzAtEOF;
  }


  /**
   * Returns the current lexical state.
   *
   * @return the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state.
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   *
   * @return the matched text.
   */
  public final String yytext() {
    return new String(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
  }


  /**
   * Returns the character at the given position from the matched text.
   *
   * <p>It is equivalent to {@code yytext().charAt(pos)}, but faster.
   *
   * @param position the position of the character to fetch. A value from 0 to {@code yylength()-1}.
   *
   * @return the character at {@code position}.
   */
  public final char yycharat(int position) {
    return zzBuffer[zzStartRead + position];
  }


  /**
   * How many characters were matched.
   *
   * @return the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * <p>In a well-formed scanner (no or only correct usage of {@code yypushback(int)} and a
   * match-all fallback rule) this method will only be called with things that
   * "Can't Possibly Happen".
   *
   * <p>If this method is called, something is seriously wrong (e.g. a JFlex bug producing a faulty
   * scanner etc.).
   *
   * <p>Usual syntax/scanner level error handling should be done in error fallback rules.
   *
   * @param errorCode the code of the error message to display.
   */
  private static void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    } catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * <p>They will be read again by then next call of the scanning method.
   *
   * @param number the number of characters to be read again. This number must not be greater than
   *     {@link #yylength()}.
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() throws java.io.IOException {
    if (!zzEOFDone) {
      zzEOFDone = true;
    
  yyclose();    }
  }

  private void agregarErrorLexico(String caracter, int linea, int columna) {
    String desc = String.format("Símbolo no reconocido: '%s'", caracter);
    if (tablaErrores != null) {
        tablaErrores.agregarError(ErrorTipo.LEXICO, desc, linea, columna);
    }
}




  /**
   * Resumes scanning until the next regular expression is matched, the end of input is encountered
   * or an I/O-Error occurs.
   *
   * @return the next token.
   * @exception java.io.IOException if any I/O-Error occurs.
   */
  @Override  public java_cup.runtime.Symbol next_token() throws java.io.IOException
  {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char[] zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      boolean zzR = false;
      int zzCh;
      int zzCharCount;
      for (zzCurrentPosL = zzStartRead  ;
           zzCurrentPosL < zzMarkedPosL ;
           zzCurrentPosL += zzCharCount ) {
        zzCh = Character.codePointAt(zzBufferL, zzCurrentPosL, zzMarkedPosL);
        zzCharCount = Character.charCount(zzCh);
        switch (zzCh) {
        case '\u000B':  // fall through
        case '\u000C':  // fall through
        case '\u0085':  // fall through
        case '\u2028':  // fall through
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn += zzCharCount;
        }
      }

      if (zzR) {
        // peek one character ahead if it is
        // (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof)
            zzPeek = false;
          else
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];


      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMap(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
            zzDoEOF();
              {
                return symbol(sym.EOF);
              }
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1:
  { 
    String desc = "Char literal con más de un carácter";
if (tablaErrores != null) {
    tablaErrores.agregarError(ErrorTipo.LEXICO, desc, (yyline+1), (yycolumn+1));
} else {
    System.err.println("Error léxico: " + desc + " en línea " + (yyline+1) + ", columna " + (yycolumn+1));
}
return symbol(sym.error);

  }

          // fall through
          case 50: break;
          case 2:
            { /* ignore */
            }
          // fall through
          case 51: break;
          case 3:
            { return symbol(sym.NOT);
            }
          // fall through
          case 52: break;
          case 4:
            { return symbol(sym.MODULO);
            }
          // fall through
          case 53: break;
          case 5:
            { return symbol(sym.LPAREN);
            }
          // fall through
          case 54: break;
          case 6:
            { return symbol(sym.RPAREN);
            }
          // fall through
          case 55: break;
          case 7:
            { return symbol(sym.MULTIPLY);
            }
          // fall through
          case 56: break;
          case 8:
            { return symbol(sym.PLUS);
            }
          // fall through
          case 57: break;
          case 9:
            { return symbol(sym.COMMA);
            }
          // fall through
          case 58: break;
          case 10:
            { return symbol(sym.MINUS);
            }
          // fall through
          case 59: break;
          case 11:
            { return symbol(sym.DIVIDE);
            }
          // fall through
          case 60: break;
          case 12:
            { return symbol(sym.INTEGER_LITERAL, Integer.parseInt(yytext()));
            }
          // fall through
          case 61: break;
          case 13:
            { return symbol(sym.COLON);
            }
          // fall through
          case 62: break;
          case 14:
            { return symbol(sym.SEMICOLON);
            }
          // fall through
          case 63: break;
          case 15:
            { return symbol(sym.LT);
            }
          // fall through
          case 64: break;
          case 16:
            { return symbol(sym.ASSIGN);
            }
          // fall through
          case 65: break;
          case 17:
            { return symbol(sym.GT);
            }
          // fall through
          case 66: break;
          case 18:
            { return symbol(sym.IDENTIFIER, yytext());
            }
          // fall through
          case 67: break;
          case 19:
            { return symbol(sym.XOR);
            }
          // fall through
          case 68: break;
          case 20:
            { return symbol(sym.LBRACE);
            }
          // fall through
          case 69: break;
          case 21:
            { return symbol(sym.RBRACE);
            }
          // fall through
          case 70: break;
          case 22:
            { return symbol(sym.NEQ);
            }
          // fall through
          case 71: break;
          case 23:
            { String text = yytext();
                                    text = text.substring(1, text.length()-1);
                                    text = unescape(text);
                                    return symbol(sym.STRING_LITERAL, text);
            }
          // fall through
          case 72: break;
          case 24:
            { return symbol(sym.AND);
            }
          // fall through
          case 73: break;
          case 25:
            { return symbol(sym.POW);
            }
          // fall through
          case 74: break;
          case 26:
            { return symbol(sym.LE);
            }
          // fall through
          case 75: break;
          case 27:
            { return symbol(sym.EQ);
            }
          // fall through
          case 76: break;
          case 28:
            { return symbol(sym.GE);
            }
          // fall through
          case 77: break;
          case 29:
            { return symbol(sym.DO);
            }
          // fall through
          case 78: break;
          case 30:
            { return symbol(sym.IF);
            }
          // fall through
          case 79: break;
          case 31:
            { return symbol(sym.OR);
            }
          // fall through
          case 80: break;
          case 32:
            { String text = yytext();
                                    text = text.substring(1, text.length()-1);
                                    text = unescape(text);
                                    if (text.length() == 1) {
                                        return symbol(sym.CHAR_LITERAL, text.charAt(0));
                                    } else {
                                        // Error: char with more than one character
                                        System.err.println("Error léxico: char literal con más de un carácter en línea " + (yyline+1) + ", columna " + (yycolumn+1));
                                        return symbol(sym.error);
                                    }
            }
          // fall through
          case 81: break;
          case 33:
            { return symbol(sym.DOUBLE_LITERAL, Double.parseDouble(yytext()));
            }
          // fall through
          case 82: break;
          case 34:
            { return symbol(sym.FOR);
            }
          // fall through
          case 83: break;
          case 35:
            { return symbol(sym.INT);
            }
          // fall through
          case 84: break;
          case 36:
            { return symbol(sym.VAR);
            }
          // fall through
          case 85: break;
          case 37:
            { return symbol(sym.BOOL);
            }
          // fall through
          case 86: break;
          case 38:
            { return symbol(sym.CASE);
            }
          // fall through
          case 87: break;
          case 39:
            { return symbol(sym.CHAR);
            }
          // fall through
          case 88: break;
          case 40:
            { return symbol(sym.ELSE);
            }
          // fall through
          case 89: break;
          case 41:
            { return symbol(sym.BOOLEAN_LITERAL, Boolean.valueOf(yytext().equalsIgnoreCase("true")));
            }
          // fall through
          case 90: break;
          case 42:
            { return symbol(sym.BREAK);
            }
          // fall through
          case 91: break;
          case 43:
            { return symbol(sym.PRINTLN);
            }
          // fall through
          case 92: break;
          case 44:
            { return symbol(sym.WHILE);
            }
          // fall through
          case 93: break;
          case 45:
            { return symbol(sym.DOUBLE);
            }
          // fall through
          case 94: break;
          case 46:
            { return symbol(sym.STRING);
            }
          // fall through
          case 95: break;
          case 47:
            { return symbol(sym.SWITCH);
            }
          // fall through
          case 96: break;
          case 48:
            { return symbol(sym.DEFAULT);
            }
          // fall through
          case 97: break;
          case 49:
            { return symbol(sym.CONTINUE);
            }
          // fall through
          case 98: break;
          default:
            zzScanError(ZZ_NO_MATCH);
        }
      }
    }
  }


}
