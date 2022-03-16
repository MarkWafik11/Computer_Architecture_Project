import java.io.*;

public class Proj {
    //memory is divided into 2 sections and we have to differentiate when using pc and storing instructions and data

    //Change register File into hashtable instead of regular list ~5 Minutes
    //Print methods
    //Clock cycles ----> Done
    //Fill instructions memory from the test file     ~30 Minutes

    static int numberOfInstructions;
    static boolean firstDecoded = true;
    static boolean firstExcuted = true;
    static int registerFile [] = new int[32];
    //static final int R0 = 0;
    static int memory[] = new int[2048];
    static int pc = 0;
    static int branchPC = 0;
    static int fetched = -1;
    static int[] decoded;
    static int[] excuted;
    static int clock;

    public static void fetch() {
        if(pc < memory.length ){
            fetched = memory[pc];
            pc++;
            System.out.println("In clock cycle number " + clock + " Instruction " + pc  + " is fetched." );
        }
    }

    public static void decode() {
        if(clock <= (2 * (numberOfInstructions-1) + 3) && fetched !=-1)
            System.out.println("In clock cycle number " + clock + " Instruction " + pc + " is decoding." );

        if(firstDecoded && clock <= (2 * (numberOfInstructions-1) + 3)){
            if(fetched != -1) {
                System.out.println("The input of this decode method is instruction number " + pc);
                int opcode = fetched & 0b11110000000000000000000000000000;
                opcode = opcode >> 28;
                opcode=0b000000000000000000000000001111&opcode;
                int r1 = fetched & 0b00001111100000000000000000000000;
                r1 = r1 >> 23;
                int r2 = fetched & 0b00000000011111000000000000000000;
                r2 = r2 >> 18;
                int r3 = fetched & 0b00000000000000111110000000000000;
                r3 = r3 >> 13;
                int shamt = fetched & 0b00000000000000000001111111111111;
                int imm = fetched & 0b00000000000000111111111111111111;
                int address = fetched & 0b00001111111111111111111111111111;
                int sign = imm | 0b00000000000000100000000000000000;
                sign = sign >>> 17;
                if (sign == 1) {
                    imm = imm | 0b11111111111111110000000000000000;
                } else {
                    imm = imm | 0b00000000000000000000000000000000;
                }

                decoded = new int[]{opcode, r1, r2, r3, shamt, imm, address,pc};
                System.out.println("The output of this decode method of instruction number " + pc +" are the following: ");
                System.out.println("The opcode is " + decoded[0] );
                System.out.println("The destination register is R" + r1);
                System.out.println("The value of register R" + r2 + "=" + registerFile[decoded[2]] );
                System.out.println("The value of register R" + r3 + "=" + registerFile[decoded[3]]);
                System.out.println("The value of shamt is " + shamt);
                System.out.println("The value of imm is " + imm);
                System.out.println("The value of address is " + address);
                System.out.println("--------------------------------------------");
            }

        }

        firstDecoded = !firstDecoded;

    }

    public static void excute() {
        if(clock <= (2 * (numberOfInstructions-1) + 5)  && decoded != null )
            System.out.println("In clock cycle number " + clock + " Instruction " + decoded[7] + " is excuting." );


        if(firstExcuted && clock <= (2 * (numberOfInstructions-1) + 5)){
            if(decoded != null){
                int opcode = decoded[0];
                int r1 = decoded[1];
                int valueR1 = registerFile[decoded[1]];
                int valueR2 = registerFile[decoded[2]];
                int valueR3 = registerFile[decoded[3]];
                int shamt = decoded[4];
                int imm = decoded[5];
                int address = decoded[6];
                int temp = -1;

                System.out.println("The input of this execute method of instruction number " + decoded[7] + " are the following:");
                System.out.println("The opcode is " + opcode );
                System.out.println("The destination register is R" + r1);
                System.out.println("The value of register R" + decoded[2] + "=" + valueR2 );
                System.out.println("The value of register R" + decoded[3] + "=" + valueR3);
                System.out.println("The value of shamt is " + decoded[4]);
                System.out.println("The value of imm is " + decoded[5]);
                System.out.println("The value of address is " + decoded[6]);
                switch(opcode){

                    //excuted = {r1,temp,writeBackFlag,readMemFlag,WriteMemFlag}
                    case 0: temp = valueR2 + valueR3; excuted = new int[]{r1,temp,1,0,0,decoded[7]};  break;//add
                    case 1: temp = valueR2 - valueR3; excuted = new int[]{r1,temp,1,0,0,decoded[7]}; break;//sub
                    case 2: temp = valueR2 * imm; excuted = new int[]{r1,temp,1,0,0,decoded[7]}; break; //muli
                    case 3: temp = valueR2 + imm; excuted = new int[]{r1,temp,1,0,0,decoded[7]}; break;//addi
                    case 4:
                        if(valueR1 != valueR2){
                            branchPC = pc - 1 ;
                            pc = pc + 1 + imm - 2;
                            numberOfInstructions = numberOfInstructions -  imm + 1;
                            fetched = -1;
                            decoded = null;
                            excuted = null;
                            int tempPC = pc + 1 ;
                            System.out.println("The output of this execution phase of instruction number "+ branchPC +" is branching to : " + tempPC);
                        }

                        break; //branch Not equal
                    case 5: temp = valueR2 & imm; excuted = new int[]{r1,temp,1,0,0,decoded[7]}; break; //andi
                    case 6: temp = valueR2 | imm; excuted = new int[]{r1,temp,1,0,0,decoded[7]}; break;//ori
                    case 7:
                        pc = pc & 0b11110000000000000000000000000000;   //1010000000000000000000000
                        address = address & 0b00001111111111111111111111111111; //0000101101111011101011
                        if(address > decoded[7]){
                            numberOfInstructions = numberOfInstructions + (decoded[7] - address) + 1 ;
                        }
                        else{
                            numberOfInstructions = numberOfInstructions + (decoded[7] - address);
                        }
                        pc = pc | address;
                        pc = pc - 1;
                        fetched = -1;
                        decoded = null;
                        excuted = null;
                        break; //j
                    case 8: temp = valueR2 << shamt; excuted = new int[]{r1,temp,1,0,0,decoded[7]}; break; //sll
                    case 9: temp = valueR2 >> shamt; excuted = new int[]{r1,temp,1,0,0,decoded[7]};break;//srl
                    case 10: temp = valueR2 + imm; excuted = new int[]{r1,temp,1,1,0,decoded[7]}; break;//lw
                    case 11: temp = valueR2 + imm; excuted = new int[]{r1,temp,0,0,1,decoded[7]}; break;//sw
                }
                if(decoded != null){
                    System.out.println("The output of this execution phase of instruction number "+decoded[7] +" is this ALU result : " + temp);
                    System.out.println("and the destination register is  "+ r1);
                }
                System.out.println("--------------------------------------------");
            }

        }
        firstExcuted = !firstExcuted;
    }

    public static void Memory() {
        if(clock > 5 && excuted != null && clock <= 2 * (numberOfInstructions -1) +  6){
            System.out.println("In clock cycle number " + clock + " Instruction " + excuted[5]   + " is using the memory.");
        }
        else if (clock > 5 && excuted == null&& clock <= 2 * (numberOfInstructions -1) +  6){
            System.out.println("In clock cycle number " + clock + " Instruction " + branchPC   + " is using the memory.");
        }


        if (excuted != null && excuted[3] == 1 && clock <= 2 * (numberOfInstructions -1) +  6) {
            System.out.println("The inputs of this Memory function of instruction number "+ excuted[5] + " are: ");
            System.out.println("The destination register:  "+ excuted[1]);
            System.out.println("The value: " + decoded[1]);
            System.out.println("The value before reading from memory: " + decoded[1]);
            excuted[1] = memory[excuted[1]];
            System.out.println("The value after reading from memory: " + decoded[1]);
        }
        else if(excuted != null && excuted[4] == 1 && clock <= 2 * (numberOfInstructions -1) +  6){
            System.out.println("The inputs of this Memory function of instruction number "+ excuted[5] + " are: ");
            System.out.println("The destination register: R"+ excuted[0]);
            System.out.println("The value: " + excuted[1]);
            System.out.println("The content of the memory at index " + excuted[1] + " before update was " + memory[excuted[1]]);
            memory[excuted[1]] = registerFile[excuted[0]];
            System.out.println("The content of the memory at index " + excuted[1] + " after update is " + memory[excuted[1]]);
            System.out.println("--------------------------------------------");

        }
    }

    public static void writeBack(){
        if(clock > 6 && excuted != null && clock <= 2 * (numberOfInstructions-1) + 7){
            System.out.println("In clock cycle number " + clock + " Instruction " + excuted[5]   + " is writing back to register.");
        }
        else if (clock > 6 && excuted == null && clock <= 2 * (numberOfInstructions-1) + 5){
            System.out.println("In clock cycle number " + clock + " Instruction " + branchPC  + " is writing back to register.");
        }

        if(excuted != null && excuted[2] == 1 && clock <= 2 * (numberOfInstructions-1) + 7 ) {
            System.out.println("The inputs of this writeBack function of instruction number "+ excuted[5] + " are: ");
            System.out.println("The destination register: R"+ excuted[0]);
            System.out.println("The value: " + excuted[1]);
            int r1 = excuted[0];
            System.out.println("The value of register R" + r1 + " before update was "+ registerFile[r1]);
            if(r1 != 0 ){
                registerFile[r1] = excuted[1];
            }
            System.out.println("The value of register R" + r1 + " after update is "+ registerFile[r1]);
            System.out.println("--------------------------------------------");
            excuted = null;

        }
    }

    public static int getInstructionsNumber(String path)  {
        int result = 0;
        File file = new File(path);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.out.println("File is not found");
        }
        while (true) {
            try {
                if (br.readLine() != null)
                    result++;
                else
                    break;
            } catch (IOException e) {
                break;
            }
        }
        return result;
    }

    public static void InstrtoBin(String path) throws IOException {

        File file = new File(path);
        BufferedReader br = null;
        int result=0;
        String R1;
        String R2;
        String R3;
        int register1;
        int register2;
        int register3;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.out.println("File is not found");
        }

        String line ;
        while((line=br.readLine())!=null) {
            String[] split1=line.split(" "); //contains everything but the opcode and the r1
            switch(split1[0].replace(" ", "")) {

                case ("ADD"): result=0b00000000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3.substring(1));
                    register3 = register3 << 13;
                    result |= register3;
                    break;

                case ("SUB"): result=0b00010000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3.substring(1));
                    register3 = register3 << 13;
                    result |= register3;

                    break;
                case ("MULI"): result=0b00100000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3);
                    result |= register3;

                    break;

                case ("ADDI"): result=0b00110000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3);
                    if(register)
                    result |= register3;

                    break;

                case ("BNE"): result=0b01000000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3);
                    result |= register3;

                    break;
                case ("ANDI"): result=0b01010000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3);
                    result |= register3;

                    break;
                case ("ORI"): result=0b01100000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3);
                    result |= register3;

                    break;
                case ("J"): result=0b01110000000000000000000000000000;

                    R3 = split1[1];
                    register3 = Integer.parseInt(R3);
                    result |= register3;

                    break;
                case ("SLL"): result=0b10000000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3);
                    result |= register3;

                    break;

                case ("SRL"): result=0b10010000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3);
                    result |= register3;

                    break;

                case ("LW"): result=0b10100000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3);
                    result |= register3;

                    break;
                case ("SW"): result=0b10110000000000000000000000000000;

                    R1 = split1[1];
                    register1 = Integer.parseInt(R1.substring(1));
                    register1 = register1 << 23;
                    result |= register1;

                    R2 = split1[2];
                    register2 = Integer.parseInt(R2.substring(1));
                    register2 = register2 << 18;
                    result |= register2;

                    R3 = split1[3];
                    register3 = Integer.parseInt(R3);
                    result |= register3;

                    break;
            }
            memory[pc]=result;
            pc++;
        }
        pc=0;
    }

    public static void printRegisters(){
        for(int i = 0 ; i < registerFile.length ; i++){
            System.out.println("Register " + i + " has value of " + registerFile[i]);
        }
    }
    public static void printmem()
    {
        for(int i=0;i<memory.length;i++)
        {
            System.out.println(memory[i]);
        }
    }

    public static void main(String [] arg)
    {
        numberOfInstructions = getInstructionsNumber("src\\test"); //number of instructions in the file   --->  9
        try {
            InstrtoBin("src\\test");
        } catch (IOException e) {
            System.out.println("File cannot be read");
        }

        clock = 1;
        while(clock <= 7 + ((numberOfInstructions - 1) * 2)){

            if(clock % 2 != 0)
                writeBack();
            if(clock % 2 == 0)
                Memory();
            excute();
            decode();
            if(clock < 2*numberOfInstructions && clock % 2 != 0)
                fetch();

            clock++;
        }
        printRegisters();
        //printmem();


    }


    //first clock cycle  fetched = value
    //2nd                fetched = new value


}